package ssn.sycon.ticketing;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ssn.sycon.ticketing.model.BuyerDetails;
import ssn.sycon.ticketing.model.ReferralData;
import ssn.sycon.ticketing.service.MainRepository;
import ssn.sycon.ticketing.utils.SharedPref;

public class PersonalActivity extends AppCompatActivity {

    ImageButton backIB;
    TextView nameTV;
    TextView totalTV;
    TextView ssniteTV;
    TextView nonSsniteTV;
    TextView generalTV;
    TextView speakerTV;
    Button logoutBT;
    private GoogleSignInClient mGoogleSignInClient;
    SwipeRefreshLayout pullToRefresh;
    ArrayList<ReferralData> referralDataArrayList;
    String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        backIB = findViewById(R.id.backIB);
        nameTV = findViewById(R.id.nameTV);
        logoutBT = findViewById(R.id.logoutBT);
        totalTV = findViewById(R.id.totalTV);
        ssniteTV = findViewById(R.id.ssniteTV);
        nonSsniteTV = findViewById(R.id.nonSsniteTV);
        speakerTV = findViewById(R.id.speakerTV);
        generalTV = findViewById(R.id.generalTV);
        pullToRefresh = findViewById(R.id.swiperfresh);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        nameTV.setText(user.getDisplayName());
        code = SharedPref.getString(PersonalActivity.this, "code");
        createRequest();
        referralUpdate();
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.setRefreshing(true);
                referralUpdate();
                pullToRefresh.setRefreshing(false);
            }
        });

        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logoutBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder loginError = new AlertDialog.Builder(PersonalActivity.this);
                View errorView = LayoutInflater.from(PersonalActivity.this).inflate(R.layout.activity_logout_prompt, null);
                Button yesBT = errorView.findViewById(R.id.yesBT);
                Button noBT = errorView.findViewById(R.id.noBT);
                loginError.setView(errorView);
                AlertDialog dialog = loginError.create();
                dialog.show();
                yesBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signOut();
                        SharedPref.putInt(PersonalActivity.this, "login", 0);
                        Intent splash = new Intent(PersonalActivity.this, SplashScreenActivity.class);
                        startActivity(splash);
                        finishAffinity();
                    }
                });
                noBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(PersonalActivity.this, gso);
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

    public void referralUpdate() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("auth");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String auth = snapshot.child("id").getValue().toString();
                String eventCode = "sycon-2021-241402";
                MainRepository.getUserDataService().user(auth, eventCode).enqueue(new Callback<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            String userData = response.body();
                            pullToRefresh.setRefreshing(true);
                            referralDataArrayList = new ArrayList<>();
                            try {
                                JSONObject jsonObject = new JSONObject(userData);
                                JSONArray jsonArray = new JSONArray((String) jsonObject.get("data"));
                                int size = 0;
                                int ssnite = 0, non = 0, general = 0,other=0;
                                size = jsonArray.length();
                                ReferralData referralData = new ReferralData();
                                totalTV.setText(String.valueOf(size));

                                int referred = 0;
                                ArrayList<BuyerDetails> buyerDetailsArrayList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject temp = jsonArray.getJSONObject(i);
                                    referred += 1;
                                    if (temp.get("allTicketName").toString().equals("SSNite exclusive Ticket")) {
                                        ssnite += 1;
                                    } else {
                                        if (temp.get("allTicketName").toString().equals("Regular Ticket (other college students)")) {
                                            non += 1;
                                        } else {
                                            if (temp.get("allTicketName").toString().equals("Corporate (General public)")) {
                                                general += 1;
                                            }
                                            else{
                                                other +=1;
                                            }
                                        }
                                    }

                                }
                                ssniteTV.setText(String.valueOf(ssnite));
                                nonSsniteTV.setText(String.valueOf(non));
                                generalTV.setText(String.valueOf(general));
                                speakerTV.setText(String.valueOf(other));
                                referralData.setBuyerDetails(buyerDetailsArrayList);
                                referralData.setReferred(referred);
                                referralDataArrayList.add(referralData);

                                Collections.sort(referralDataArrayList, Collections.reverseOrder());
//                                for (ReferralData r : referralDataArrayList) {
//                                    if (r.getCode().toString().equals(code)) {
//                                        ArrayList<BuyerDetails> buyerDetailsArrayList = r.getBuyerDetails();
//                                        int size=0;
//                                        size=buyerDetailsArrayList.size();
//                                        totalTV.setText(String.valueOf(size));
//                                        int ssnite = 0, non = 0, general = 0;
//                                        for (BuyerDetails b : buyerDetailsArrayList) {
//                                            if (b.getTicketname().equals("SSNite exclusive Ticket")) {
//                                                ssnite += 1;
//                                            } else {
//                                                if (b.getTicketname().equals("Regular Ticket (other college students)")) {
//                                                    non += 1;
//                                                } else {
//                                                    if (b.getTicketname().equals("Corporate (General public)")) {
//                                                        general += 1;
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        ssniteTV.setText(String.valueOf(ssnite));
//                                        nonSsniteTV.setText(String.valueOf(non));
//                                        generalTV.setText(String.valueOf(general));
//                                    }
//                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                pullToRefresh.setRefreshing(false);
                            }
                            pullToRefresh.setRefreshing(false);
                        } else {
                            AlertCustomDialogBox(PersonalActivity.this, "Error", "Something went wrong. Please try again!!");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        AlertCustomDialogBox(PersonalActivity.this, "No Internet connection", "Please try again!!");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AlertCustomDialogBox(PersonalActivity.this, "Database Error", "Auth key expired");
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