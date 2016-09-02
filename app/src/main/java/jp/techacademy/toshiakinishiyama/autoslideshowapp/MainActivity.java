package jp.techacademy.toshiakinishiyama.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button btnNext;
    Button btnBack;
    Button btnAuto;
    private int mImageNum;
    private int mImageMax;
    private Uri[] uriImageList;
    private boolean mAutoFlg;        // スライドショーフラグ（ true : 実行中、false : 非実行）
    private Timer timer;
    private Handler handler = new Handler();
    private int showTime = 2000;     // スライドショー表示時間の間隔 [msec]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        btnAuto = (Button) findViewById(R.id.btnAuto);
        btnAuto.setOnClickListener(this);

        // メンバ変数の初期化
        mImageNum = 0;
        mImageMax = 0;
        mAutoFlg = false;       // スライドショー 非実行

        // パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Android 6.0以降の場合
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                // 許可されている
                getContentsInfo();
            }
            else
            {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }
        else
        {
            // Android 5系以下の場合
            getContentsInfo();
        }
    }

    // 許可ダイアログの結果
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // 許可された（特に何もしない）
                }
                else
                {
                    // 許可されなかった
                    MessageDialog dialogFragment = MessageDialog.newInstance("エラー", "許可されなかったのでアプリを終了します。");
                    dialogFragment.show(getFragmentManager(), "dialog_fragment");
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

    // ボタンクリック
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNext)
        {
            // 次の画像を表示する
            ShowImageNext();
        }
        else if (v.getId() == R.id.btnBack)
        {
            // 前の画像を表示する
            ShowImageBack();
        }
        else if (v.getId() == R.id.btnAuto)
        {
            if(mAutoFlg == false)
            {
                // スライドショーを開始する
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                if(mImageNum == mImageMax - 1)
                                {
                                    // 最後の画像の場合は先頭に戻る
                                    mImageNum = 0;
                                }
                                else
                                {
                                    // 次の画像
                                    mImageNum++;
                                }
                                ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                                imageVIew.setImageURI(uriImageList[mImageNum]);
                            }
                        });
                    }
                }, 0, showTime);

                // ボタン表示を変更する
                btnAuto.setText("停止");
                mAutoFlg = true;
            }
            else
            {
                // スライドショーを停止する
                timer.cancel();
                timer = null;

                // ボタン表示を変更する
                btnAuto.setText("再生");
                mAutoFlg = false;
            }
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        mImageMax = cursor.getCount();
        uriImageList = new Uri[mImageMax];
        int cnt = 0;

        // ギャラリー内に保存されている画像の URI を全て取得する
        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                uriImageList[cnt] = imageUri;
                cnt++;

                Log.d("ANDROID", "URI : " + imageUri.toString());
            } while (cursor.moveToNext());
        }
        cursor.close();

        // 最初の画像を表示する（起動時）
        mImageNum = 0;      // 明示的に初期化
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(uriImageList[mImageNum]);
    }

    // 次の画像を表示する
    private void ShowImageNext()
    {
        // 次の画像を表示する
        if(mImageNum == mImageMax - 1)
        {
            // 現在表示している画像が最後の場合は最初に戻る
            mImageNum = 0;
        }
        else
        {
            mImageNum++;
        }
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(uriImageList[mImageNum]);
    }

    // 前の画像を表示する
    private void ShowImageBack()
    {
        // 次の画像を表示する
        if(mImageNum == 0)
        {
            // 現在表示している画像が最初の場合は最後に進む
            mImageNum = mImageMax - 1;
        }
        else
        {
            mImageNum--;
        }
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(uriImageList[mImageNum]);
    }
}
