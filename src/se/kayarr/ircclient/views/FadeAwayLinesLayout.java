package se.kayarr.ircclient.views;

import java.util.List;

import se.kayarr.ircclient.irc.output.OutputLine;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FadeAwayLinesLayout extends ViewGroup {
	public static final String TAG = FadeAwayLinesLayout.class.getSimpleName();
	
	private LinearLayout mainList;
	
//	volatile private long lastViewAddTime = 0;
//	
//	private class TriggerViewCleanupRunnable implements Runnable {
//		
//		private long addTime;
//		
//		public TriggerViewCleanupRunnable(long addTime) {
//			this.addTime = addTime;
//		}
//		
//		public void run() {
//			
//			if(addTime != lastViewAddTime) return;
//			
//			Log.d(TAG, "*** Time to check if there any views outside");
//			
//			triggerRemovingOutsideViews();
//			
//		}
//		
//	}
	
	public FadeAwayLinesLayout(Context context) {
		this(context, null);
	}
	
	private void setLayoutTransitionForList(LinearLayout list) {
		
//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			LayoutTransition trans = new LayoutTransition();
//			
//			trans.addTransitionListener(new LayoutTransition.TransitionListener() {
//				
//				public void startTransition(LayoutTransition transition,
//						ViewGroup container, View view, int transitionType) {
//				}
//				
//				public void endTransition(LayoutTransition transition, ViewGroup container,
//						View view, int transitionType) {
//					
//					//Log.d(TAG, "endTransition triggered with " + transitionType);
//					
//					if(transitionType == LayoutTransition.CHANGE_APPEARING) {
//						
//						//Log.d(TAG, "endTransition triggered with CHANGE_APPEARING");
//						
//						lastViewAddTime = System.currentTimeMillis();
//						
//						container.postDelayed(
//								new TriggerViewCleanupRunnable(lastViewAddTime),
//								2000);
//					}
//				}
//			});
//			
//			mainList.setLayoutTransition(trans);
//		}
		
	}

	public FadeAwayLinesLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mainList = new LinearLayout(context);
		mainList.setLayoutParams(
				new FadeAwayLinesLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT
				)
		);
		mainList.setOrientation(LinearLayout.VERTICAL);
		setLayoutTransitionForList(mainList);
		
		addView(mainList);
		
		mainList.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			public void onGlobalLayout() {
				
				triggerRemovingOutsideViews();
			}
			
		});
	}
	
	public void addExistingLines(List<OutputLine> lines) {
		for(OutputLine line : lines) {
			addLine(line);
		}
	}
	
	public void addLine(OutputLine line) {
		TextView textLine = new TextView( getContext() );
		
		textLine.setLayoutParams(
				new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT,
						Gravity.BOTTOM
				)
		);
		
		textLine.setText(line.getOutput());
		
		textLine.setTextColor( getContext().getResources().getColor(android.R.color.primary_text_dark) );
		
		mainList.addView(textLine);
	}
	
	public void clearLines() {
		mainList.removeAllViews();
	}
	
	private volatile int viewCount = 0;
	
	private void triggerRemovingOutsideViews() {
		
		if(viewCount == mainList.getChildCount()) return;
		
		Log.d(TAG, mainList.hashCode() + " triggered view check");
		
		for(int i = mainList.getChildCount() - 1; i >= 0; i--) {
			View child = mainList.getChildAt(i);
			
//			Log.d(TAG, "Child #" + i + ": " +
//					"getHeight() = " + getHeight() + ", " +
//					"mainList.getHeight() = " + mainList.getHeight() + ", " +
//					"mainList.getMeasuredHeight() = " + mainList.getMeasuredHeight() + ", " +
//					"child.getTop() = " + child.getTop() + ", " +
//					"child.getBottom() = " + child.getBottom()
//			);
			
//			Log.d(TAG, "^--- dist is " + (mainList.getHeight() - child.getTop()) );
//			Log.d(TAG, "^--- dist is " + (mainList.getHeight() - child.getBottom()) );
			
			if( mainList.getHeight() - child.getBottom() > getHeight() ) {
				int removeCount = i + 1;
				
				Log.d(TAG, "Child " + i + " is outside, remove " + removeCount + " items from beginning");
				
				mainList.removeViews(0, removeCount);
				
				viewCount = mainList.getChildCount();
				
				break;
			}
		}
		
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
        measureChildren(
        		MeasureSpec.makeMeasureSpec( MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST ),
        		MeasureSpec.makeMeasureSpec( 0, MeasureSpec.UNSPECIFIED )
        );
        
        setMeasuredDimension(
        		MeasureSpec.getSize(widthMeasureSpec),
        		MeasureSpec.getSize(heightMeasureSpec)
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t,
            int r, int b) {
    	
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                int childLeft = getPaddingLeft();
                int childBottom = getMeasuredHeight() - getPaddingBottom();
                
                int childTop = childBottom - ( getPaddingTop() + child.getMeasuredHeight() );
                int childRight = childLeft + child.getMeasuredWidth() - getPaddingRight();
                
                child.layout(childLeft, childTop, childRight, childBottom);

            }
        }
    }
    
    public static int resolveSizeAndStateCompat(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
	        case MeasureSpec.UNSPECIFIED:
	            result = size;
	            break;
	        case MeasureSpec.AT_MOST:
	            if (specSize < size) {
	                result = specSize | MEASURED_STATE_TOO_SMALL;
	            } else {
	                result = size;
	            }
	            break;
	        case MeasureSpec.EXACTLY:
	            result = specSize;
	            break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }
}
