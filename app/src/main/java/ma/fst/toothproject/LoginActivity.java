package ma.fst.toothproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText cpswd,username;


    TextView forgot;
    Button add;

    String userName,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        cpswd=findViewById(R.id.editTextPassword);
        add=findViewById(R.id.buttonLogin);
        username=findViewById(R.id.editTextUsername);





        add.setOnClickListener(this);
    }




    @Override
    public void onClick(View v) {
        userName = username.getText().toString();
        password = cpswd.getText().toString();
        System.out.println(userName+password);
        String url = "http://128.10.4.113:8087/api/v1/Students/login";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userName", userName);
            jsonBody.put("password", password);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                JSONObject jsonObject= response;
                                long id = jsonObject.getInt("id");
                                String email=jsonObject.getString("email");
                                String username=jsonObject.getString("userName");
                                String nom=jsonObject.getString("lastName");
                                String prenom=jsonObject.getString("firstName");
                                if (!jsonObject.isNull("photo")) {
                                    byte[] photoBytes = Base64.decode(response.getString("photo"), Base64.DEFAULT);
                                    // Convertir les octets de l'image en Bitmap
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);

                                    // Convertir le Bitmap en tableau d'octets
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();
                                    intent.putExtra("bitmap", bitmapByteArray);
                                }

                                JSONObject groupe = jsonObject.getJSONObject("groupe");
                                String code=groupe.getString("code");
                                intent.putExtra("studentid",id);
                                intent.putExtra("code", code);
                                intent.putExtra("email", email);
                                intent.putExtra("nom", nom);
                                intent.putExtra("prenom", prenom);
                                intent.putExtra("username", username);
                                startActivity(intent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(LoginActivity.this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
                        }
                    }
            );


            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}