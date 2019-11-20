package com.github.anrimian.musicplayer.ui.common.dialogs.input;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Arguments.CAN_BE_EMPTY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_HINT;
import static com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_VALUE;
import static com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.HINTS_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.NEGATIVE_BUTTON_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.POSITIVE_BUTTON_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.TITLE_ARG;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setSoftInputVisible;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setEditableText;
import static com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher.onTextChanged;

public class InputTextDialogFragment extends DialogFragment {

    @BindView(R.id.edit_text)
    AutoCompleteTextView editText;

    @Nullable
    private Callback<String> onCompleteListener;

    @Nullable
    private BiCallback<String, Bundle> complexCompleteListener;

    public static InputTextDialogFragment newInstance(@StringRes int title,
                                                      @StringRes int positiveButtonText,
                                                      @StringRes int negativeButtonText,
                                                      @StringRes int editTextHint,
                                                      String editTextValue) {
        return newInstance(title,
                positiveButtonText,
                negativeButtonText,
                editTextHint,
                editTextValue,
                true);
    }

    public static InputTextDialogFragment newInstance(@StringRes int title,
                                                      @StringRes int positiveButtonText,
                                                      @StringRes int negativeButtonText,
                                                      @StringRes int editTextHint,
                                                      String editTextValue,
                                                      boolean canBeEmpty) {
        return newInstance(title,
                positiveButtonText,
                negativeButtonText,
                editTextHint,
                editTextValue,
                canBeEmpty,
                null);
    }

    public static InputTextDialogFragment newInstance(@StringRes int title,
                                                      @StringRes int positiveButtonText,
                                                      @StringRes int negativeButtonText,
                                                      @StringRes int editTextHint,
                                                      String editTextValue,
                                                      boolean canBeEmpty,
                                                      Bundle extra) {
        Bundle args = new Bundle();
        args.putInt(TITLE_ARG, title);
        args.putInt(POSITIVE_BUTTON_ARG, positiveButtonText);
        args.putInt(NEGATIVE_BUTTON_ARG, negativeButtonText);
        args.putInt(EDIT_TEXT_HINT, editTextHint);
        args.putString(EDIT_TEXT_VALUE, editTextValue);
        args.putBoolean(CAN_BE_EMPTY_ARG, canBeEmpty);
        args.putBundle(EXTRA_DATA_ARG, extra);
        InputTextDialogFragment fragment = new InputTextDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_common_input_simple, null);

        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        assert args != null;
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(args.getInt(TITLE_ARG))
                .setPositiveButton(args.getInt(POSITIVE_BUTTON_ARG), null)
                .setNegativeButton(args.getInt(NEGATIVE_BUTTON_ARG), (dialog1, which) -> {})
                .setView(view)
                .create();
        setSoftInputVisible(dialog.getWindow());
        dialog.show();

        editText.setHint(args.getInt(EDIT_TEXT_HINT));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            onCompleteButtonClicked();
            return true;
        });
        String startText = args.getString(EDIT_TEXT_VALUE);
        setEditableText(editText, startText);

        String[] hints = args.getStringArray(HINTS_ARG);
        if (hints != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.select_dialog_singlechoice,
                    hints);
            editText.setAdapter(adapter);
        }

        editText.requestFocus();

        Button btnCreate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCreate.setOnClickListener(v -> onCompleteButtonClicked());

        if (!args.getBoolean(CAN_BE_EMPTY_ARG)) {
            btnCreate.setEnabled(!isEmpty(startText));
            onTextChanged(editText, text -> btnCreate.setEnabled(!isEmpty(text)));
        }

        return dialog;
    }

    public void setOnCompleteListener(@Nullable Callback<String> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setComplexCompleteListener(@Nullable BiCallback<String, Bundle> complexCompleteListener) {
        this.complexCompleteListener = complexCompleteListener;
    }

    private void onCompleteButtonClicked() {
        String text = editText.getText().toString();
        if (!TextUtils.equals(text, getArguments().getString(EDIT_TEXT_VALUE))) {
            if (onCompleteListener != null) {
                onCompleteListener.call(text);
            }
            if (complexCompleteListener != null) {
                complexCompleteListener.call(text, getArguments().getBundle(EXTRA_DATA_ARG));
            }
        }
        dismiss();
    }

    public static class Builder {
        @StringRes private final int title;
        @StringRes private final int positiveButtonText;
        @StringRes private final int negativeButtonText;
        @StringRes private final int editTextHint;
        private final String editTextValue;
        private boolean canBeEmpty = true;
        private Bundle extra = null;
        private String[] hints;

        public Builder(int title,
                       int positiveButtonText,
                       int negativeButtonText,
                       int editTextHint,
                       String editTextValue) {
            this.title = title;
            this.positiveButtonText = positiveButtonText;
            this.negativeButtonText = negativeButtonText;
            this.editTextHint = editTextHint;
            this.editTextValue = editTextValue;
        }

        public Builder canBeEmpty(boolean canBeEmpty) {
            this.canBeEmpty = canBeEmpty;
            return this;
        }

        public Builder extra(Bundle extra) {
            this.extra = extra;
            return this;
        }

        public Builder hints(String[] hints) {
            this.hints = hints;
            return this;
        }

        public InputTextDialogFragment build() {
            Bundle args = new Bundle();
            args.putInt(TITLE_ARG, title);
            args.putInt(POSITIVE_BUTTON_ARG, positiveButtonText);
            args.putInt(NEGATIVE_BUTTON_ARG, negativeButtonText);
            args.putInt(EDIT_TEXT_HINT, editTextHint);
            args.putString(EDIT_TEXT_VALUE, editTextValue);
            args.putBoolean(CAN_BE_EMPTY_ARG, canBeEmpty);
            args.putBundle(EXTRA_DATA_ARG, extra);
            args.putStringArray(HINTS_ARG, hints);
            InputTextDialogFragment fragment = new InputTextDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }
    }

}
