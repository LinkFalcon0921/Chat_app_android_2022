package com.flintcore.chat_app_android_22.utilities.views;

import android.text.InputFilter;

public interface DefaultConfigs {
    interface InputFilters{
        InputFilter NAME_INPUT_FILTER = new InputFilter.LengthFilter(25);
        InputFilter EMAIL_INPUT_FILTER = new InputFilter.LengthFilter(30);
        InputFilter PASS_INPUT_FILTER = new InputFilter.LengthFilter(25);
        InputFilter MESSAGE_INPUT_FILTER = new InputFilter.LengthFilter(100);
    }

}
