package com.zhongli.john.iblackboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
//    private static final String[] DUMMY_CREDENTIALS = new String[]{
//            "foo@example.com:hello", "bar@example.com:world"
//    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText mPasswordView;
    private EditText IPaddrView;
    private EditText chatPortView;
    private EditText audioPortView;
    private EditText picPortView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();
        IPaddrView = (EditText) findViewById(R.id.text_IP);
        chatPortView = (EditText) findViewById(R.id.text_chat_port);
        audioPortView = (EditText) findViewById(R.id.text_audio_port);
        picPortView = (EditText) findViewById(R.id.text_picture_port);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(1);
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
//        signInButton.setError("asdasd");
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(1);
            }
        });


        Button regButton = (Button) findViewById(R.id.reg_button);
        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(2);
            }
        });


    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(int type) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(type, IPaddrView.getText().toString().trim(), Integer.parseInt(chatPortView.getText().toString()), Integer.parseInt(audioPortView.getText().toString()), Integer.parseInt(picPortView.getText().toString()), username, password);
            mAuthTask.execute((Void) null);


        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        usernameView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {


        private final String IPaddr;
        private final int chatPort;
        private final int audioPort;
        private final int picPort;
        private final String name;
        private final String pwd;
        private NetClient conn;
        private int type;

        UserLoginTask(int type, String IP, int chatPort, int audioPort, int picPort, String username, String password) {
            this.type = type;
            this.IPaddr = IP;
            this.chatPort = chatPort;
            this.audioPort = audioPort;
            this.picPort = picPort;
            this.name = username;
            this.pwd = password;
        }

        // 登录事件处理
        private boolean logAction() {
            // 1.创建连接对象
            conn = new NetClient(null,IPaddr, chatPort, audioPort, picPort);
            // 2.连接服务器
            if (conn.isconnect()) {// 如果能连接
                // 3.登陆
                int key = conn.istrue(name, pwd);
                if (key == 1) {
                    // 4.

                    // 5.启动接收线程
//                    conn.start();
                    Bundle bundle = new Bundle();
                    bundle.putString("IPaddr", IPaddr);
                    bundle.putInt("ChatPort", chatPort);
                    bundle.putInt("AudioPort", audioPort);
                    bundle.putInt("PicPort", picPort);
                    bundle.putString("UserName",name);
                    bundle.putString("PassWord",pwd);
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, mainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    try {
                        conn.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity
                    // 关闭登陆界面 跳转
                    return true;
                } else if (key == 2) {
                    //javax.swing.JOptionPane.showMessageDialog(null,
                    //   "Password is wrong..");
                    System.out.println("Password is wrong..");
                    return false;
                } else {
                    // javax.swing.JOptionPane.showMessageDialog(null,
                    //  "Please register first..");
                    return false;
                }

            } else {
                // javax.swing.JOptionPane.showMessageDialog(null, "Server Error..");
                System.out.println("Server Error..");
                return false;
            }
        }

        // 注册事件处理
        private boolean regAction() {
            // 1.创建连接对象
            conn = new NetClient(null,IPaddr, chatPort, audioPort, picPort);
            // 2.连接服务器
            if (conn.isconnect()) {// 如果能连接
                // 3.注册
                boolean can = conn.canReg(name, pwd);
                if (can) {
//                    javax.swing.JOptionPane.showMessageDialog(null,
//                            "Registration is successful..");
                    return true;
                } else {
//                    javax.swing.JOptionPane.showMessageDialog(null,
//                            "Username already exists..");
                    return false;
                }

            } else {
//                javax.swing.JOptionPane.showMessageDialog(null,
//                        "Connect to the server failed..");

                System.out.println("Connect to the server failed..");
                System.out.println(IPaddr);
                return false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                conn = new NetClient(null,IPaddr, chatPort, audioPort, picPort);
                // Thread.sleep(2000);
            } catch (Exception e) {
                return false;
            }

            //            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(user)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            // TODO: register the new account here.
            if (type == 1) {
                return logAction();
            } else {
                System.out.println();
                return regAction();

            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;


            if (success) {
//                finish();
                System.out.println("Success");
                if (type != 1) {
                    new AlertDialog.Builder(mPasswordView.getContext())
                            .setTitle("Message")
                            .setMessage("Success")
                            .setPositiveButton("OK", null)
                            .show();
                }
            } else {
                mPasswordView.setError(getString(R.string.error_reg_first));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
}



