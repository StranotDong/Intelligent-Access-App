package com.example.lock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class VideoSurfaceView extends SurfaceView implements Callback {

	private SurfaceHolder sfh;
	private Canvas canvas;
	private int screen_heigth;
	private int screen_width;
	private String addressUrl = "http://192.168.191.3:8090/?action=stream"; // Default
	
	// 图片帧速
	private AtomicInteger temp_fps = new AtomicInteger(0);
	private AtomicInteger current_fps = new AtomicInteger(0);

	// 下载速度KB/S
	private AtomicInteger temp_speed = new AtomicInteger(0);
	private AtomicInteger current_speed = new AtomicInteger(0);

	private HttpURLConnection urlConn = null;
	
	private boolean threadstop = false;

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		setFocusable(true);

		// 创建定时器定时更新视频流帧速和下载速度
		new Timer().schedule(new TimerTask() {
			public void run() {
				temp_fps.set(current_fps.get());
				temp_speed.set(current_speed.get());

				current_fps.set(0);
				current_speed.set(0);
				// Log.e("Timer", temp_fps.get() + ", " + temp_speed.get());
			}
		}, 1000, 1000);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.i("MontiorSurfaceView", "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		screen_width = dm.widthPixels;
		screen_heigth = dm.heightPixels;

		new DrawVideo().start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.i("MontiorSurfaceView", "surfaceDestroyed");
		try {
			// TODO: 需要做安全的关闭下载线程和界面线程
			threadstop = true;
			urlConn.disconnect();
		} catch (Exception e) {

		}
	}

	public void setMontiorAddress(String address) {
		addressUrl = address;
		Log.i("addressUrl", addressUrl);
	}

	// 下载jpg线程
	public class DownloadJPG implements Runnable {
		private BlockingQueue queue;

		public DownloadJPG(BlockingQueue queue) {
			this.queue = queue;
		}

		public void run() {
			// jpg图片缓冲区， 128KB
			final int jpgbufSize = 128 * 1024;
			byte[] jpg_buf = new byte[jpgbufSize];

			final int headerLen = 128;

			URL url = null;

			// 使用HTTPURLConnetion打开连接
			try {
				url = new URL(addressUrl);
				urlConn = (HttpURLConnection) url.openConnection();

			} catch (IOException ex) {
				ex.printStackTrace();
				Log.e("DownloadJPG", addressUrl + "连接失败");
				return;
			}

			Log.i("DownloadJPG线程", addressUrl + "连接成功");

			int status = 0;
			int pos = 0;
			int jpgFrameLen = 0;

			while (!threadstop) {

				// ------------ JPG HEADER ------------
				// Content-Type: image/jpeg\r\n
				// Content-Length: %d\r\n
				// X-Timestamp: %d.%06d\r\n
				// ... JPG Frame ...
				// \r\n--boundarydonotcross\r\n

				try {
					pos += urlConn.getInputStream().read(jpg_buf, pos, jpgbufSize - pos);
				} catch (Exception e) {
					// 数据接收失败，断开连接，退出
					Log.e("DownloadJPG线程", "数据接收失败，断开连接，退出");
					urlConn.disconnect();
					e.printStackTrace();

					return;
				}

				switch (status) {
				case 0: // header parse
					if (pos > headerLen) {
						//
						// 128字节数据包包含header和jpg数据帧
						// 开始提取jpg数据帧的大小，并确定jpg数据帧开始位置
						//
						// System.out.println("header parse: " + pos);
						final String contentLenString = "Content-Length: ";
						String header;
						try {
							header = new String(jpg_buf, 0, headerLen, "utf-8");
							// printHexString(jpg_buf, headerLen);
							// Log.i("--", header);
							// Log.i("-------------", "--------------------");
							int index = header.indexOf(contentLenString);
							if (index != -1) {
								int index2 = header.indexOf("\r\n", index);
								if (index2 != -1) {
									String frameLenString = header.substring(index + contentLenString.length(), index2);

									// System.out.println("jpg数据帧长度： " +
									// frameLenString);

									try {
										jpgFrameLen = Integer.parseInt(frameLenString.trim());////////////////////////////////////////
									} catch (NumberFormatException e) {
										System.out.println("解析jpg数据帧长度失败 : " + frameLenString);
										pos = 0;
										continue;
									}
								}

								// OK, find JPG frame
								// JPG图片编码参考：
								// http://www.cnblogs.com/leaven/archive/2010/04/06/1705846.html
								int i = 0;
								for (i = 0; i < headerLen; i++) {
									if (jpg_buf[i] == (byte) 0xFF) {
										// 找到jpg图片帧头，将之前的HTTP头部去除，jpg_buf中只存放jpg数据帧
										System.arraycopy(jpg_buf, i, jpg_buf, 0, pos - i);
										pos = pos - i;
										status++;
										break;
									}
								}

								if (i == headerLen) {
									Log.w("---- lock  ----", "jpg数据帧错误，没有0xFF帧头");
									pos = 0;
								}
							} else {
								Log.w("---- lock ----", "header error");
								pos = 0;
							}
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
							Log.e("---- lock  ----", "UnsupportedEncodingException");
						}
					}
					break;

				case 1: // jpg数据帧处理
					if (pos >= jpgFrameLen) {

						// 一张jpg数据帧接收完成
						// 将图片数据保存到队列中
						Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_buf, 0, jpgFrameLen));
						if (bmp != null) {
							int width = screen_width;
							int height = screen_heigth;

							float rate_width = (float) screen_width / (float) bmp.getWidth();
							float rate_height = (float) screen_heigth / (float) bmp.getHeight();

							if (rate_width > rate_height)
								width = (int) ((float) bmp.getWidth() * rate_height);
							if (rate_width < rate_height)
								height = (int) ((float) bmp.getHeight() * rate_width);

							Bitmap bitmap = Bitmap.createScaledBitmap(bmp, width, height, false);

							try {
								Log.i("---", "jpg_len = " + jpgFrameLen);
								if (!queue.add(bitmap))
									Log.e("---", "queue.add error");
							} catch (Exception e) {
								// TODO: handle exception
								Log.e("----DownloadJPG Thread ----", "queue.add(jpgString) error");
							}
						}

						current_speed.addAndGet(jpgFrameLen);

						// 重置图片缓存指针
						pos = pos - jpgFrameLen;
						System.arraycopy(jpg_buf, jpgFrameLen, jpg_buf, 0, pos);
						// 准备接收下一个jpg
						status = 0;
					}
					break;
				default:
					break;
				}
			}
		}
	}

	// 刷新surfaceView界面线程
	class DrawVideo extends Thread {
		public DrawVideo() {
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		public void run() {
			String str_fps = String.format("FPS: [%2d] - SP: [%4d]KB/s", temp_fps.get(), temp_speed.get());
			Bitmap data = null;
			final int TEXT_SEZE = 25;
			Paint pt = new Paint();
			pt.setAntiAlias(true);
			pt.setColor(Color.GREEN);
			pt.setTextSize(TEXT_SEZE);
			pt.setStrokeWidth(1);
			pt.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

			BlockingQueue<Bitmap> queue = new LinkedBlockingQueue<Bitmap>(32);
			DownloadJPG downloadJPG = new DownloadJPG(queue);

			ExecutorService service = Executors.newCachedThreadPool();
			service.execute(downloadJPG);
			service.shutdown();

			while (!threadstop) {

				try {
					// 从队列中取出数据帧，无数据会在此休眠
					// Thread.sleep(100 * 1000);
					data = queue.take();
					if (queue.size() != 0)
						Log.w("~~~", "queue_len = " + queue.size());

				} catch (InterruptedException e) {
					Log.w("---- queue ----", "queue.take() error");
				}

				if (data != null) {
					int width = data.getWidth();
					int height = data.getHeight();

					// 显示图像，按照图片实际长宽比显示，不作全屏拉伸
					canvas = sfh.lockCanvas();
					canvas.drawColor(Color.BLACK);

					canvas.drawBitmap(data, (screen_width - width) / 2, (screen_heigth - height) / 2, null);
					current_fps.addAndGet(1);
					str_fps = String.format("FPS: [%2d] - SP: [%4d]KB/s", temp_fps.get(), temp_speed.get() / 1024);
					canvas.drawText(str_fps, 2, TEXT_SEZE + 2, pt);

					// 画完一副图像，解锁画布
					sfh.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}


