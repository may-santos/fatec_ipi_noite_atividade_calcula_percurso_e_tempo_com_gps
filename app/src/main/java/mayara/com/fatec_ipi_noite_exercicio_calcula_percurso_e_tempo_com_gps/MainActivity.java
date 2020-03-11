package mayara.com.fatec_ipi_noite_exercicio_calcula_percurso_e_tempo_com_gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
  private static final int GPS_REQUEST_CODE = 1001;

  private LocationManager locationManager; // vale null
  private LocationListener locationListener;
  private TextView distanciaPercorridaTextView;
  private Chronometer chrono;
  private Button permissaoGpsButton;
  private Button ativarGpsButton;
  private Button desativarGpsButton;
  private Button iniciarButton;
  private Button terminarButton;
  private EditText searchEditText;
  private FloatingActionButton searchButton;

  private double lat;
  private double lon;
  private double distancia = 0d;
  private boolean isPercurso = false, iniciouPercurso = false, gpsAtivado = false;
  private Location localAnt;

  private void configurarGPS() {

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) { //push - pois recebe o dado que vai ser enviado.

        lat = location.getLatitude();
        lon = location.getLongitude();

        if (iniciouPercurso) {
          if (isPercurso) {
            localAnt = location;
            isPercurso = false;
          }

          double distanciaAnt = localAnt.distanceTo(location);
          distancia += distanciaAnt;

          String formatoKM = String.format(Locale.getDefault(), "%.2f Km", distancia / 1000d); // transformando em KM.
          distanciaPercorridaTextView.setText(formatoKM);

          localAnt = location;
        }
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {}

      @Override
      public void onProviderEnabled(String provider) {}

      @Override
      public void onProviderDisabled(String provider) {}
    };
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    distanciaPercorridaTextView = findViewById(R.id.distanciaPercorridaTextView);
    searchEditText = findViewById(R.id.searchEditText);
    chrono = findViewById(R.id.tempoPassadoChronometer);
    permissaoGpsButton = findViewById(R.id.permissaoGpsButton);
    ativarGpsButton = findViewById(R.id.ativarGpsButton);
    desativarGpsButton = findViewById(R.id.desativarGpsButton);
    iniciarButton = findViewById(R.id.iniciarButton);
    terminarButton = findViewById(R.id.terminarButton);
    searchButton = findViewById(R.id.searchButton);

    configurarGPS();

    permissaoGpsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(MainActivity.this, getString(R.string.permissao_ja_concedida), Toast.LENGTH_SHORT).show();
        } else {
          ActivityCompat.requestPermissions(
            MainActivity.this,
            new String [] { Manifest.permission.ACCESS_FINE_LOCATION},
            GPS_REQUEST_CODE
          );
        }
      }
    });

    ativarGpsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          if (gpsAtivado) {
            Toast.makeText(MainActivity.this, getString(R.string.gps_ja_ativado), Toast.LENGTH_SHORT).show();

          } else {
            locationManager.requestLocationUpdates(
              LocationManager.GPS_PROVIDER,
              0,
              0,
              locationListener
            );
            Toast.makeText(MainActivity.this, getString(R.string.gps_ativado), Toast.LENGTH_SHORT).show();
            gpsAtivado = true;
          }
        } else {
          Toast.makeText(MainActivity.this, getString(R.string.feedback_no_gps), Toast.LENGTH_SHORT).show();
        }
      }
    });

    desativarGpsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          if (gpsAtivado) {
            if (iniciouPercurso) { // estou verificando se o usuário está no percurso, caso esteja, ele será notificado que primeiramente é necessário Terminar o percurso para desativar o GPS.
              Toast.makeText(MainActivity.this, getString(R.string.termine_o_percurso), Toast.LENGTH_SHORT).show();
            } else {
              locationManager.removeUpdates(locationListener);
              gpsAtivado = false;
              Toast.makeText(MainActivity.this, getString(R.string.gps_desativado), Toast.LENGTH_SHORT).show();
            }
          } else {
            Toast.makeText(MainActivity.this, getString(R.string.gps_nao_ativado), Toast.LENGTH_SHORT).show();
          }
        }
      }
    });

    iniciarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (gpsAtivado) {
          if (iniciouPercurso) {
            Toast.makeText(MainActivity.this, getString(R.string.percurso_ja_ativado), Toast.LENGTH_SHORT).show();
          } else {
            isPercurso = iniciouPercurso = true;
            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start();
            Toast.makeText(MainActivity.this, getString(R.string.percurso_iniciado), Toast.LENGTH_SHORT).show();
          }
        } else {
          Toast.makeText(MainActivity.this, getString(R.string.gps_nao_ativado), Toast.LENGTH_SHORT).show();
        }
      }
    });

    terminarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (iniciouPercurso) {
          chrono.stop();
          Toast.makeText(MainActivity.this, chrono.getText() +" - " + distanciaPercorridaTextView.getText(), Toast.LENGTH_SHORT).show();
          chrono.setText("");
          distanciaPercorridaTextView.setText("0 Km");
          iniciouPercurso = false;
        } else {
          Toast.makeText(MainActivity.this, getString(R.string.nao_percurso), Toast.LENGTH_SHORT).show();
        }
      }
    });

    searchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Uri uri =
          Uri.parse(String.format(Locale.getDefault(), "geo:%f,%f?q="+searchEditText.getText(), lat, lon));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
        searchEditText.setText("");
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    if (requestCode == GPS_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0,
            locationListener
          );
          Toast.makeText(this, getString(R.string.permissao_concedida), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(MainActivity.this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
        }
      }
    }
  }
}
