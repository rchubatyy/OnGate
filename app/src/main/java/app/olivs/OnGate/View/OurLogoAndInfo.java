package app.olivs.OnGate.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import app.olivs.OnGate.Activities.RegisterActivity;
import app.olivs.OnGate.Activities.UserPinActivity;

public class OurLogoAndInfo extends AppCompatImageView {


    public OurLogoAndInfo(@NonNull final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!(context instanceof UserPinActivity) && !(context instanceof RegisterActivity)) {
                    ((Activity)context).finish();
                }
                return true;
            }
        });
    }
}
