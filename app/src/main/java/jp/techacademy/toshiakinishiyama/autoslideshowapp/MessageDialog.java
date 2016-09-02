package jp.techacademy.toshiakinishiyama.autoslideshowapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * メッセージダイアログを表示するクラス
 * Created by Toshiaki.Nishiyama on 2016/09/01.
 */
public class MessageDialog extends DialogFragment
{

    public static MessageDialog newInstance(String title, String message) {
        MessageDialog fragment = new MessageDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle safedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null);

        return builder.create();
    }
}
