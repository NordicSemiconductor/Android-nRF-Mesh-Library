package no.nordicsemi.android.nrfmeshprovisioner.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.meshprovisioner.AddressRange;
import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.Range;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class RangeView extends View {

    private Paint mPaint;
    private int unallocatedColor;
    private int rangesColor;
    private int otherRangeColor;
    private int conflictColor;
    private int borderColor;
    private List<Range> ranges = new ArrayList<>();
    private List<Range> otherRanges = new ArrayList<>();

    public RangeView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public void addRanges(@NonNull final List<? extends Range> ranges) {
        this.ranges.addAll(ranges);
        invalidate();
    }

    public void addRange(@NonNull final Range range) {
        ranges.add(range);
        invalidate();
    }

    public void clearRanges() {
        ranges.clear();
        invalidate();
    }

    public void addOtherRanges(@NonNull final List<? extends Range> otherRanges) {
        this.otherRanges.addAll(otherRanges);
        invalidate();
    }

    public void addOtherRange(@NonNull final Range otherRange) {
        otherRanges.add(otherRange);
        invalidate();
    }

    public void clearOtherRanges() {
        otherRanges.clear();
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(unallocatedColor);
        drawRanges(canvas);
        drawOtherRanges(canvas);
        drawConflictingRange(canvas);
        drawBorder(canvas);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initPaint(final Context context) {
        unallocatedColor = ContextCompat.getColor(context, R.color.nordicLightGray);
        rangesColor = ContextCompat.getColor(context, R.color.nordicLake);
        otherRangeColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        conflictColor = ContextCompat.getColor(context, R.color.nordicRed);
        borderColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        mPaint = new Paint();
    }

    private void drawRanges(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(rangesColor);
        for (Range range : ranges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(canvas, range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    private void drawOtherRanges(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(otherRangeColor);
        for (Range range : otherRanges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(canvas, range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    public void drawConflictingRange(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(conflictColor);
        final Rect rect = canvas.getClipBounds();
        for (Range range : ranges) {
            for (Range other : otherRanges) {
                if (range instanceof AllocatedUnicastRange) {
                    final AllocatedUnicastRange unicastRange = (AllocatedUnicastRange) range;
                    final AllocatedUnicastRange otherRange = (AllocatedUnicastRange) other;
                    final float unit = (rect.width() / (float) (MeshAddress.END_UNICAST_ADDRESS - MeshAddress.START_UNICAST_ADDRESS));
                    final Rect overlapRegion = getOverlappingAddressRegion(unicastRange, otherRange, unit, rect.height());
                    if (overlapRegion != null) {
                        canvas.drawRect(overlapRegion, paint);
                    }
                } else if (range instanceof AllocatedGroupRange) {
                    final AllocatedGroupRange groupRange = (AllocatedGroupRange) range;
                    final AllocatedGroupRange otherRange = (AllocatedGroupRange) other;
                    final float unit = (rect.width() / (float) (MeshAddress.END_GROUP_ADDRESS - MeshAddress.START_GROUP_ADDRESS));
                    final Rect overlapRegion = getOverlappingAddressRegion(groupRange, otherRange, unit, rect.height());
                    if (overlapRegion != null) {
                        canvas.drawRect(overlapRegion, paint);
                    }
                } else {
                    final AllocatedSceneRange sceneRange = (AllocatedSceneRange) range;
                    final AllocatedSceneRange otherRange = (AllocatedSceneRange) other;
                    float unit = (rect.width() / (float) (range.getUpperBound() - range.getLowerBound()));
                    final Rect overlapRegion = getOverlappingSceneRegion(sceneRange, otherRange, unit, rect.height());
                    if (overlapRegion != null) {
                        canvas.drawRect(overlapRegion, paint);
                    }
                }
            }
        }
    }

    private void drawBorder(final Canvas canvas) {
        mPaint.setColor(borderColor);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(canvas.getClipBounds(), mPaint);
    }

    private Rect getRegion(@NonNull final Canvas canvas, final int lowAddress, final int highAddress, final int lowerBound, final int upperBound) {
        final Rect mRect = canvas.getClipBounds();
        final float unit = (mRect.width() / (float) (upperBound - lowerBound));
        final int x = (int) ((lowAddress - lowerBound) * unit);
        final int right = (int) ((highAddress - lowerBound) * unit);
        return new Rect(x, 0, right, mRect.height());
    }

    private Paint getRectPaint() {
        final Paint p = mPaint;
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private Rect getOverlappingAddressRegion(@NonNull final AddressRange range, @NonNull final AddressRange otherRange, final float unit, final int height) {
        int x;
        int right;
        // Are the ranges are equal
        if (range.getLowAddress() == otherRange.getLowAddress() && range.getHighAddress() == otherRange.getHighAddress()) {
            x = (int) ((range.getLowAddress() - otherRange.getLowerBound()) * unit);
            right = (int) ((range.getHighAddress() - otherRange.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range greater than the other range?
        else if (range.getLowAddress() < otherRange.getLowAddress() && range.getHighAddress() > otherRange.getHighAddress()) {
            x = (int) ((otherRange.getLowAddress() - otherRange.getLowerBound()) * unit);
            right = (int) ((otherRange.getHighAddress() - otherRange.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range within the other range?
        else if (range.getLowAddress() > otherRange.getLowAddress() && range.getHighAddress() < otherRange.getHighAddress()) {
            x = (int) ((range.getLowAddress() - range.getLowerBound()) * unit);
            right = (int) ((range.getHighAddress() - range.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range's lower address lower than the other range's low address
        else if (range.getLowAddress() <= otherRange.getLowAddress() &&
                range.getHighAddress() >= otherRange.getLowAddress() && range.getHighAddress() <= otherRange.getHighAddress()) {
            x = (int) ((otherRange.getLowAddress() - range.getLowerBound()) * unit);
            right = (int) (range.getHighAddress() * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range's higher address greater than the other range's high address
        else if (range.getHighAddress() >= otherRange.getHighAddress() &&
                range.getLowAddress() >= otherRange.getLowAddress() && range.getLowAddress() <= otherRange.getHighAddress()) {
            x = (int) ((range.getLowAddress() - otherRange.getLowerBound()) * unit);
            right = (int) ((otherRange.getHighAddress() - range.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        return null;
    }

    private Rect getOverlappingSceneRegion(@NonNull final AllocatedSceneRange range, @NonNull final AllocatedSceneRange otherRange, final float unit, final int height) {
        int x;
        int right;
        // Are the ranges are equal
        if (range.getFirstScene() == otherRange.getFirstScene() && range.getLastScene() == otherRange.getLastScene()) {
            x = (int) ((range.getFirstScene() - otherRange.getLowerBound()) * unit);
            right = (int) ((range.getLastScene() - otherRange.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range greater than the other range?
        else if (range.getFirstScene() < otherRange.getFirstScene() && range.getLastScene() > otherRange.getLastScene()) {
            x = (int) ((otherRange.getFirstScene() - otherRange.getLowerBound()) * unit);
            right = (int) ((otherRange.getLastScene() - otherRange.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range within the other range?
        else if (range.getFirstScene() > otherRange.getFirstScene() && range.getLastScene() < otherRange.getLastScene()) {
            x = (int) ((range.getFirstScene() - range.getLowerBound()) * unit);
            right = (int) ((range.getLastScene() - range.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range's lower address lower than the other range's low address
        else if (range.getFirstScene() <= otherRange.getFirstScene() &&
                range.getLastScene() >= otherRange.getFirstScene() && range.getLastScene() <= otherRange.getLastScene()) {
            x = (int) ((otherRange.getFirstScene() - range.getLowerBound()) * unit);
            right = (int) (range.getLastScene() * unit);
            return new Rect(x, 0, right, height);
        }
        // Is the range's higher address greater than the other range's high address
        else if (range.getLastScene() >= otherRange.getLastScene() &&
                range.getFirstScene() >= otherRange.getFirstScene() && range.getFirstScene() <= otherRange.getLastScene()) {
            x = (int) ((range.getFirstScene() - otherRange.getLowerBound()) * unit);
            right = (int) ((otherRange.getLastScene() - range.getLowerBound()) * unit);
            return new Rect(x, 0, right, height);
        }
        return null;
    }
}
