package ssn.sycon.ticketing;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ssn.sycon.ticketing.adapter.ReferralCodeAdapter;
import ssn.sycon.ticketing.model.BuyerDetails;
import ssn.sycon.ticketing.model.ReferralData;
import ssn.sycon.ticketing.service.MainRepository;
import ssn.sycon.ticketing.utils.Constants;

public class MainActivity extends AppCompatActivity {
    RecyclerView referralRV;
    ReferralCodeAdapter referralCodeAdapter;
    ArrayList<ReferralData> referralDataArrayList;
    TextInputEditText searchTIET;
    ImageView noDataFoundTV;
    SwipeRefreshLayout pullToRefresh;
    ImageButton backIB;
    private GoogleSignInClient mGoogleSignInClient;
    TextView toolBarTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        referralRV = findViewById(R.id.referralRV);
        backIB = findViewById(R.id.backIB);
        searchTIET = findViewById(R.id.searchTIET);
        toolBarTV = findViewById(R.id.toolBarTV);
        noDataFoundTV = findViewById(R.id.noDataFoundTV);
        pullToRefresh = findViewById(R.id.swiperfresh);
        referralCodeAdapter = new ReferralCodeAdapter(new ArrayList<>(), MainActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        referralRV.setLayoutManager(linearLayoutManager);
        referralRV.setAdapter(referralCodeAdapter);
        createRequest();
        referralUpdate();
        toolBarTV.setSelected(true);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.setRefreshing(true);
                referralUpdate();
                pullToRefresh.setRefreshing(false);
            }
        });

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);


        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        pullToRefresh.setRefreshing(true);
        referralUpdate();
        pullToRefresh.setRefreshing(false);
    }

    public void referralUpdate() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("auth");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String auth =snapshot.child("id").getValue().toString();
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
                                for (Map.Entry<Integer, String> entry : Constants.referralCode.entrySet()) {
                                    ReferralData referralData = new ReferralData();
                                    referralData.setName(entry.getValue());
                                    referralData.setCode(entry.getKey());
                                    int referred = 0;
                                    ArrayList<BuyerDetails> buyerDetailsArrayList = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject temp = jsonArray.getJSONObject(i);
                                        if (temp.has("customAnswer345798") && temp.get("customAnswer345798").equals(entry.getKey().toString())) {
                                            BuyerDetails buyerDetails = new BuyerDetails();
                                            buyerDetails.setName(temp.get("userName").toString());
                                            buyerDetails.setTicketname(temp.get("allTicketName").toString());
                                            buyerDetails.setPrice((Double) temp.get("ticketPrice"));
                                            buyerDetails.setOrderId(temp.get("uniqueOrderId").toString());
                                            buyerDetails.setEmail(temp.get("userEmailId").toString());
                                            buyerDetailsArrayList.add(buyerDetails);
                                            referred += 1;
                                        }
                                    }
                                    referralData.setBuyerDetails(buyerDetailsArrayList);
                                    referralData.setReferred(referred);
                                    referralDataArrayList.add(referralData);
                                }
                                Collections.sort(referralDataArrayList, Collections.reverseOrder());
                                referralCodeAdapter.setReferralDataArrayList(referralDataArrayList);
                                ArrayList<String> top = new ArrayList<>();
                                for (int i = 0; i < 3; i++) {
                                    top.add(referralDataArrayList.get(i).getCode().toString());
                                }
                                referralCodeAdapter.setTop(top);
                                referralCodeAdapter.notifyDataSetChanged();
                                filter(referralDataArrayList);
                                pullToRefresh.setRefreshing(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                pullToRefresh.setRefreshing(false);
                            }
                            pullToRefresh.setRefreshing(false);
                        } else {
                            AlertCustomDialogBox(MainActivity.this, "Error", "Something went wrong. Please try again!!");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        AlertCustomDialogBox(MainActivity.this, "No Internet connection", "Please try again!!");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AlertCustomDialogBox(MainActivity.this, "Database Error", "Auth key expired");
            }
        });

    }

    void filter(ArrayList<ReferralData> referralDataArrayList) {
        searchTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ArrayList<ReferralData> searchList = new ArrayList<>();
                for (ReferralData d : referralDataArrayList) {
                    if (d.getCode().toString().contains(s.toString()) || d.getName().toLowerCase().contains(s.toString())) {
                        searchList.add(d);
                    }
                }
                if (searchList.isEmpty()) {
                    noDataFoundTV.setVisibility(View.VISIBLE);
                    referralRV.setVisibility(View.GONE);
                } else {
                    noDataFoundTV.setVisibility(View.GONE);
                    referralRV.setVisibility(View.VISIBLE);
                }
                referralCodeAdapter.setReferralDataArrayList(searchList);
                referralCodeAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
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