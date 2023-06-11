package com.example.velmurugan.getcurrentlatitudeandlongitudeandroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Classe principal que representa a Atividade principal da aplicação.
 */
public class MainActivity extends AppCompatActivity {

    private GpsTracker gpsTracker;
    private TextView tvLatitude, tvLongitude, tvVelocidadeMediaParcial, tvVelocidadeMediaTotal, tvTempoDeslocamento, tvDistanciaPercorrida, tvConsumoCombustivelTotal, tvTempoParaDestinoFinal, tvVelocidadeRecomendada;
    private Veiculo veiculo;
    private Handler handler;
    private Runnable runnable;
    private boolean percursoIniciado = false;
    private LocationThread locationThread;

    /**
     * Método chamado quando a atividade é criada.
     *
     * @param savedInstanceState o estado anteriormente salvo da atividade, ou null se nenhuma
     *                           informação foi salva.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa os elementos de interface
        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);
        tvVelocidadeMediaParcial = findViewById(R.id.velocidadeMediaParcial);
        tvVelocidadeMediaTotal = findViewById(R.id.velocidadeMediaTotal);
        tvTempoDeslocamento = findViewById(R.id.tempoDeslocamento);
        tvDistanciaPercorrida = findViewById(R.id.distanciaPercorrida);
        tvConsumoCombustivelTotal = findViewById(R.id.consumoCombustivelTotal);
        tvTempoParaDestinoFinal = findViewById(R.id.tempoParaDestinoFinal);
        tvVelocidadeRecomendada = findViewById(R.id.velocidadeRecomendada);

        // Solicita permissão de localização
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configura o botão "Iniciar Percurso"
        Button btnIniciarPercurso = findViewById(R.id.btnIniciarPercurso);
        btnIniciarPercurso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define percursoIniciado como true
                percursoIniciado = true;
            }
        });

        // Configura o botão "Reiniciar"
        Button btnReiniciar = findViewById(R.id.btnReiniciar);
        btnReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reiniciarAplicativo();
            }
        });

        // Inicializa o handler e o runnable para atualização contínua dos dados de localização
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (getPercursoIniciado()) {
                    getLocation();
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    /**
     * Obtém o valor da variável percursoIniciado.
     *
     * @return true se o percurso foi iniciado, false caso contrário.
     */
    public boolean getPercursoIniciado() {
        return percursoIniciado;
    }

    /**
     * Reinicia a aplicação.
     */
    private void reiniciarAplicativo() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Método chamado quando a atividade está em primeiro plano e interage com o usuário.
     * Inicia a execução da função getLocation.
     */
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 1000); // Inicia a execução da função após 1 segundo
        startLocationThread();
    }

    /**
     * Método chamado quando a atividade perde o foco e está prestes a ser pausada.
     * Interrompe a execução da função getLocation.
     */
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Interrompe a execução da função
        stopLocationThread();
    }

    /**

     Inicia a execução de uma nova thread responsável por rastrear a localização.
     Se a thread já estiver em execução, nenhum novo thread é criado.
     */
    private void startLocationThread() {
        if (locationThread == null || !locationThread.isAlive()) {
            locationThread = new LocationThread();
            locationThread.start();
        }
    }
    /**

     Interrompe a execução da thread responsável por rastrear a localização.
     Se a thread estiver em execução, ela é interrompida e o objeto thread é definido como nulo.
     */
    private void stopLocationThread() {
        if (locationThread != null) {
            locationThread.interrupt();
            locationThread = null;
        }
    }

    /**
     * Obtém a localização atual e atualiza os dados do veículo com base nas coordenadas.
     */
    public void getLocation() {
        gpsTracker = new GpsTracker(MainActivity.this);
        LocationData locationData = gpsTracker.getLocation();
        if (locationData != null) {
            double latitude = locationData.getLatitude();
            double longitude = locationData.getLongitude();
            long timestamp = locationData.getTimestamp();
            tvLatitude.setText(String.valueOf(latitude));
            tvLongitude.setText(String.valueOf(longitude));

            // Atualiza os dados do veículo com base nas coordenadas
            if (veiculo == null) {
                veiculo = new Veiculo(gpsTracker);
            }
            veiculo.atualizarDados(latitude, longitude, timestamp);
            startLocationThread();

            // Obtem os dados atualizados do veículo
            double velocidadeMediaParcial = veiculo.getVelocidadeMediaParcial();
            double velocidadeMediaTotal = veiculo.getVelocidadeMediaTotal();
            long tempoDeslocamento = veiculo.getTempoDeslocamento();
            double distanciaPercorrida = veiculo.getDistanciaPercorrida();
            double consumoCombustivelTotal = veiculo.getConsumoCombustivelTotal();
            long tempoParaDestinoFinal = veiculo.getTempoParaDestinoFinal();
            double velocidadeRecomendada = veiculo.getVelocidadeRecomendada();

            // Atualiza as TextViews com os valores calculados
            tvVelocidadeMediaParcial.setText(String.valueOf(velocidadeMediaParcial));
            tvVelocidadeMediaTotal.setText(String.valueOf(velocidadeMediaTotal));
            tvTempoDeslocamento.setText(String.valueOf(tempoDeslocamento));
            tvDistanciaPercorrida.setText(String.valueOf(distanciaPercorrida));
            tvConsumoCombustivelTotal.setText(String.valueOf(consumoCombustivelTotal));
            tvTempoParaDestinoFinal.setText(String.valueOf(tempoParaDestinoFinal));
            tvVelocidadeRecomendada.setText(String.valueOf(velocidadeRecomendada));

            if (latitude > -20.4569 && longitude < -45.8358) {
                // Verifica as condições para exibir as mensagens
                if (tempoParaDestinoFinal >= -10 && tempoParaDestinoFinal <= 10) {
                    tvTempoParaDestinoFinal.setText(String.valueOf(tempoParaDestinoFinal));
                    exibirResultado("Você concluiu o percurso no tempo correto!", android.R.color.holo_green_light);
                } else if (tempoParaDestinoFinal < -10) {
                    tvTempoParaDestinoFinal.setText(String.valueOf(tempoParaDestinoFinal));
                    exibirResultado("Você atrasou!", android.R.color.holo_red_light);
                } else if (tempoParaDestinoFinal > 10 && tempoParaDestinoFinal <= 100) {
                    tvTempoParaDestinoFinal.setText(String.valueOf(tempoParaDestinoFinal));
                    exibirResultado("Você adiantou!", android.R.color.holo_red_light);
                }
            }
        }
    }

    /**
     * Exibe o resultado do percurso em um AlertDialog personalizado.
     *
     * @param mensagem   a mensagem a ser exibida.
     * @param corFundo   a cor de fundo do AlertDialog.
     */
    private void exibirResultado(String mensagem, int corFundo) {
        // Cria um novo layout
        View resultadoView = getLayoutInflater().inflate(R.layout.layout_result, null);
        TextView tvResultado = resultadoView.findViewById(R.id.tvResultado);
        tvResultado.setText(mensagem);
        tvResultado.setBackgroundResource(corFundo);

        // Adiciona o botão de reiniciar e define o comportamento de clique
        Button btnReiniciar = resultadoView.findViewById(R.id.btnReiniciar);
        btnReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reiniciarAplicativo();
            }
        });

        // Exibe o novo layout em um AlertDialog personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(resultadoView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Esta classe é responsável por executar em uma thread separada a obtenção da localização.
     */
    private class LocationThread extends Thread {
        @Override
        public void run() {
            while (getPercursoIniciado()) {
                getLocation();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

