package com.rayzem.demowaze;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FloatingBubbleService extends Service {
    private WindowManager windowManager;
    private RelativeLayout floatingBubbleView, removeBubbleView;

    private ImageView floatingBubbleImg, removeBubbleImg;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private boolean isServiceRunning = false;
    private boolean isLeft = true;

    @SuppressWarnings("deprecation")

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void handleStart() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        removeBubbleView = (RelativeLayout) inflater.inflate(R.layout.remove_bubble, null);
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        removeBubbleView.setVisibility(View.GONE);
        removeBubbleImg = (ImageView) removeBubbleView.findViewById(R.id.remove_img);
        windowManager.addView(removeBubbleView, paramRemove);


        floatingBubbleView = (RelativeLayout) inflater.inflate(R.layout.bubble, null);
        floatingBubbleImg = (ImageView) floatingBubbleView.findViewById(R.id.floatingBubble);

        windowManager.getDefaultDisplay().getSize(szWindow);

		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			windowManager.getDefaultDisplay().getSize(szWindow);
		} else {
			int w = windowManager.getDefaultDisplay().getWidth();
			int h = windowManager.getDefaultDisplay().getHeight();
			szWindow.set(w, h);
		}*/

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        windowManager.addView(floatingBubbleView, params);

        floatingBubbleView.setOnTouchListener(new View.OnTouchListener() {
            long timeStart = 0, timeEnd = 0;
            boolean isLongClick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handlerLongClick = new Handler();
            Runnable runnableLongClick = new Runnable() {

                @Override
                public void run() {

                    isLongClick = true;
                    removeBubbleView.setVisibility(View.VISIBLE);
                    floatingBubbleLongclick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) floatingBubbleView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeStart = System.currentTimeMillis();
                        handlerLongClick.postDelayed(runnableLongClick, 600);

                        remove_img_width = removeBubbleImg.getLayoutParams().width;
                        remove_img_height = removeBubbleImg.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;


                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));

                                if (removeBubbleImg.getLayoutParams().height == remove_img_height) {
                                    removeBubbleImg.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    removeBubbleImg.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    windowManager.updateViewLayout(removeBubbleView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeBubbleView.getWidth() - floatingBubbleView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeBubbleView.getHeight() - floatingBubbleView.getHeight())) / 2;

                                windowManager.updateViewLayout(floatingBubbleView, layoutParams);
                                break;
                            } else {
                                inBounded = false;
                                removeBubbleImg.getLayoutParams().height = remove_img_height;
                                removeBubbleImg.getLayoutParams().width = remove_img_width;

                                WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
                                int x_cord_remove = (szWindow.x - removeBubbleView.getWidth()) / 2;
                                int y_cord_remove = szWindow.y - (removeBubbleView.getHeight() + getStatusBarHeight());

                                param_remove.x = x_cord_remove;
                                param_remove.y = y_cord_remove;

                                windowManager.updateViewLayout(removeBubbleView, param_remove);
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(floatingBubbleView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeBubbleView.setVisibility(View.GONE);
                        removeBubbleImg.getLayoutParams().height = remove_img_height;
                        removeBubbleImg.getLayoutParams().width = remove_img_width;
                        handlerLongClick.removeCallbacks(runnableLongClick);

                        if (inBounded) {
                            stopService(new Intent(FloatingBubbleService.this, FloatingBubbleService.class));
                            isServiceRunning = false;
                            inBounded = false;
                            break;
                        }


                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            timeEnd = System.currentTimeMillis();
                            if ((timeEnd - timeStart) < 300) {
                                floatingBubbleClicked();
                                stopService(new Intent(FloatingBubbleService.this, FloatingBubbleService.class));
                                isServiceRunning = false;
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (floatingBubbleView.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (floatingBubbleView.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;

                        inBounded = false;
                        resetPosition(x_cord);

                        break;
                    default:
                        break;
                }
                return true;
            }
        });


    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (windowManager == null)
            return;

        windowManager.getDefaultDisplay().getSize(szWindow);

		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            int w = windowManager.getDefaultDisplay().getWidth();
            int h = windowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }*/

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) floatingBubbleView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + (floatingBubbleView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (floatingBubbleView.getHeight() + getStatusBarHeight());
                windowManager.updateViewLayout(floatingBubbleView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);

        } else {
            isLeft = false;
            moveToRight(x_cord_now);

        }

    }

    private void moveToLeft(final int x_cord_now) {
        final int x = szWindow.x - x_cord_now;

        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floatingBubbleView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = 0 - (int) (double) bounceValue(step, x);
                windowManager.updateViewLayout(floatingBubbleView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;
                windowManager.updateViewLayout(floatingBubbleView, mParams);
            }
        }.start();
    }

    private void moveToRight(final int x_cord_now) {
        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floatingBubbleView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - floatingBubbleView.getWidth();
                windowManager.updateViewLayout(floatingBubbleView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - floatingBubbleView.getWidth();
                windowManager.updateViewLayout(floatingBubbleView, mParams);
            }
        }.start();
    }

    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    private void floatingBubbleClicked() {
        Intent it = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(it);

    }

    private void floatingBubbleLongclick() {

        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
        int x_cord_remove = (szWindow.x - removeBubbleView.getWidth()) / 2;
        int y_cord_remove = szWindow.y - (removeBubbleView.getHeight() + getStatusBarHeight());

        param_remove.x = x_cord_remove;
        param_remove.y = y_cord_remove;

        windowManager.updateViewLayout(removeBubbleView, param_remove);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (startId == Service.START_STICKY) {
            handleStart();
            return super.onStartCommand(intent, flags, startId);
        } else {
            return Service.START_NOT_STICKY;
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (floatingBubbleView != null) {
            windowManager.removeView(floatingBubbleView);
        }

        if (removeBubbleView != null) {
            windowManager.removeView(removeBubbleView);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


}
