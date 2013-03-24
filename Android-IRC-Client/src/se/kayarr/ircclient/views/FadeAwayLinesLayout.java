package se.kayarr.ircclient.views;

import java.util.LinkedList;
import java.util.List;

import se.kayarr.ircclient.irc.output.OutputLine;
import android.annotation.SuppressLint;
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
	
	/**
	 * A temporary list of all lines to show. This is to keep the list
	 * until after the layout has been measured.
	 */
	private List<OutputLine> tempLineList;
	private List<TextView> viewsToAdd = new LinkedList<TextView>();
	
	public FadeAwayLinesLayout(Context context) {
		this(context, null);
	}
	
	@SuppressLint("NewApi")
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
		
		mainList.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {
			
			public void onGlobalLayout() {
				
				triggerRemovingOutsideViews();
			}
			
		} );
	}

	public void addExistingLines(List<OutputLine> lines) {
		tempLineList = lines;
	}
	
	private TextView obtainViewForLine(OutputLine line) {
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
		
		return textLine;
	}
	
	public void addLine(OutputLine line) {
		TextView textLine = obtainViewForLine(line);
		
		addViewToList(textLine);
	}
	
	private void addViewToList(View view) {
		mainList.addView(view);
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
    	
//    	Log.d(TAG, "********* onMeasure called (" +
//    			"w = " + MeasureSpec.toString(widthMeasureSpec) + ", " +
//    			"h = " + MeasureSpec.toString(heightMeasureSpec) + ")");
    	
        measureChildren(
        		MeasureSpec.makeMeasureSpec( MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY ),
        		MeasureSpec.makeMeasureSpec( 0, MeasureSpec.UNSPECIFIED )
        );
        
        //This is where we start adding lines, as the list has been measured and
        //we know how large it wants to be.
        //Log.d(TAG, "Measured width of list is " + mainList.getMeasuredWidth());
        
        if(tempLineList != null && mainList.getMeasuredWidth() > 0) {
        	
        	int totalHeight = 0;
        	
    		for(int i = tempLineList.size()-1; i >= 0; i--) {
    			TextView textLine = obtainViewForLine( tempLineList.get(i) );
    			
    			textLine.measure(
    					MeasureSpec.makeMeasureSpec(mainList.getMeasuredWidth(), MeasureSpec.AT_MOST),
    					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    			);
    			
//    			Log.d(TAG, "Line \"" + tempLineList.get(i).getOutput() + "\": " +
//    					"The child's height is " + textLine.getMeasuredHeight());
    			
    			totalHeight += textLine.getMeasuredHeight();
    			
//    			Log.d(TAG, "totalHeight is now " + totalHeight + ", and height is " + MeasureSpec.getSize(heightMeasureSpec));
    			
    			viewsToAdd.add(textLine);
    			
    			if(totalHeight > MeasureSpec.getSize(heightMeasureSpec)) break;
    		}
    		
//			Log.d(TAG, "Done with measuring items; " +
//					viewsToAdd.size() + " items are to be added");
			
			for(TextView view : viewsToAdd) {
				mainList.addView(view, 0);
			}
			viewsToAdd.clear();
			
			tempLineList = null;
        }
        
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
