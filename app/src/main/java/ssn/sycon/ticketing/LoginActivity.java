package ssn.sycon.ticketing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.HashMap;
import java.util.Map;

import ssn.sycon.ticketing.utils.SharedPref;


public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 222;
    GoogleSignInButton signInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    Map<String, String> emailId;
    SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.sign_in_button);
        mAuth = FirebaseAuth.getInstance();
        pullToRefresh = findViewById(R.id.swiperfresh);
        createRequest();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pullToRefresh.setRefreshing(true);
                signIn();
                pullToRefresh.setRefreshing(false);
            }
        });

    }


    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                pullToRefresh.setRefreshing(true);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                pullToRefresh.setRefreshing(false);

            } catch (ApiException e) {
                pullToRefresh.setRefreshing(false);
                AlertCustomDialogBox(LoginActivity.this, "Invalid Credentials", "Entered username or password is invalid or unauthorized. Please try again!!");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pullToRefresh.setRefreshing(true);
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            emailId = new HashMap<String, String>();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        if (ds.child("code") != null && ds.child("email") != null) {
                                            emailId.put(ds.child("code").getValue().toString(), ds.child("email").getValue().toString());
                                        }
                                    }
                                    if (emailId.containsValue(user.getEmail())) {
                                        String code=null;
                                        for (Map.Entry<String, String> entry : emailId.entrySet()) {
                                            if(entry.getValue().equals(user.getEmail())){
                                                code=entry.getKey();
                                                break;
                                            }
                                        }
                                        pullToRefresh.setRefreshing(false);
                                        SharedPref.putInt(LoginActivity.this, "login", 1);
                                        SharedPref.putString(LoginActivity.this, "code",code);
                                        Intent main = new Intent(LoginActivity.this, MenuActivity.class);
                                        startActivity(main);
                                        finishAffinity();
                                    } else {
                                        pullToRefresh.setRefreshing(false);
                                        AlertCustomDialogBox(LoginActivity.this, "Invalid Credentials", "Unauthorized Access");
                                        SharedPref.putInt(LoginActivity.this, "login", 0);
                                        signOut();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    pullToRefresh.setRefreshing(false);
                                }
                            });
                        } else {
                            pullToRefresh.setRefreshing(false);
                            AlertCustomDialogBox(LoginActivity.this, "No Internet connection", "Please try again!!");
                            SharedPref.putInt(LoginActivity.this, "login", 0);
                        }
                    }
                });
    }


    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                    }
                });
    }

    void AlertCustomDialogBox(Context context, String error, String message) {
        AlertDialog.Builder loginError = new AlertDialog.Builder(context);
        View errorView = LayoutInflater.from(context).inflate(R.layout.activity_login_prompt, null);
        TextView errorTV = errorView.findViewById(R.id.errorTV);
        TextView msgTV = errorView.findViewById(R.id.msgTV);
        Button tryAgainBT = errorView.findViewById(R.id.tryAgainBT);
        loginError.setView(errorView);
        AlertDialog dialog = loginError.create();
        errorTV.setText(error);
        msgTV.setText(message);
        dialog.show();
        tryAgainBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}