package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.internal.ExtraActionPickerDialog;

public class MessageFormLayout extends LinearLayout {

  protected ActionListener actionListener;
  protected ViewGroup composer;

  private View btnExtra;
  private View btnSubmit;
  private List<MessageExtraActionItemPresenter> extraActionItems;

  public MessageFormLayout(Context context) {
    super(context);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  public final void setOnActionListener(@Nullable ActionListener listener) {
    actionListener = listener;
  }

  private void init() {
    extraActionItems = new ArrayList<>();
    composer = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(R.layout.message_composer, this, false);

    btnExtra = composer.findViewById(R.id.btn_extras);

    btnExtra.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showExtraActionSelectionDialog();
      }
    });

    btnSubmit = composer.findViewById(R.id.btn_submit);

    btnSubmit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String messageText = getText();
        if (messageText.length() > 0 && actionListener != null) {
          actionListener.onSubmitText(messageText);
        }
      }
    });

    btnSubmit.setScaleX(0);
    btnSubmit.setScaleY(0);
    btnSubmit.setVisibility(GONE);

    ((EditText) composer.findViewById(R.id.editor)).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (TextUtils.getTrimmedLength(s) > 0) {
          animateHide(btnExtra);
          animateShow(btnSubmit);
        } else {
          animateShow(btnExtra);
          animateHide(btnSubmit);
        }
      }
    });

    addView(composer);
  }

  public void addExtraActionItem(MessageExtraActionItemPresenter itemPresenter) {
    boolean found = false;
    for (MessageExtraActionItemPresenter item : extraActionItems) {
      if (item.getItemId() == itemPresenter.getItemId()) {
        found = true;
        break;
      }
    }

    if (!found) {
      extraActionItems.add(itemPresenter);
    }
  }

  private TextView getEditor() {
    return (TextView) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(CharSequence text) {
    getEditor().setText(text);
  }

  public void setEnabled(boolean enabled) {
    getEditor().setEnabled(enabled);
    composer.findViewById(R.id.btn_submit).setEnabled(enabled);
  }

  private void animateHide(final View view) {
    view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(GONE);
      }
    });
  }

  private void animateShow(final View view) {
    view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(VISIBLE);
      }
    });
  }

  private void showExtraActionSelectionDialog() {
    ExtraActionPickerDialog.showAsTask(getContext(), extraActionItems)
        .onSuccess(new Continuation<Integer, Object>() {
          @Override
          public Object then(Task<Integer> task) throws Exception {
            int which = task.getResult();
            if (actionListener != null) {
              actionListener.onExtraActionSelected(which);
            }
            return null;
          }
        });
  }

  public interface ActionListener {
    void onSubmitText(String message);

    void onExtraActionSelected(int itemId);
  }
}
