package com.example.coldstartdesk;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter.Change;

import static com.example.coldstartdesk.LoginController.showMenssage;

public class UsuarioController {


    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtCpf;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private void onVoltarButtonClick(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("menu-view.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    public void initialize() {
        // Aplica máscara visual para CPF enquanto digita (formato 000.000.000-00)
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 11) return null;
            return change;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        txtCpf.setTextFormatter(textFormatter);

        txtCpf.textProperty().addListener((obs, oldV, newV) -> {
            String digits = newV.replaceAll("\\D", "");
            String formatted = formatCpf(digits);
            if (!newV.equals(formatted)) {
                txtCpf.setText(formatted);
            }
        });
    }

    private String formatCpf(String digits) {
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            sb.append(digits.charAt(i));
            if (i == 2 || i == 5) sb.append('.');
            if (i == 8) sb.append('-');
        }
        return sb.toString();
    }

    @FXML
    private void onSalvarButtonClick(ActionEvent event)  throws IOException{
        // Validação separada para cada campo obrigatório
        String nome = txtNome.getText() != null ? txtNome.getText().trim() : "";
        if (nome.isEmpty()){
            showMenssage("Digite o nome", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        if (email.isEmpty()){
            showMenssage("Digite o email", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }
        if (!email.contains("@")){
            showMenssage("Informe um email válido contendo '@'", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        String cpfRaw = txtCpf.getText() != null ? txtCpf.getText().replaceAll("\\D", "").trim() : "";
        if (cpfRaw.isEmpty()){
            showMenssage("Digite o CPF", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        String senha = txtSenha.getText() != null ? txtSenha.getText().trim() : "";
        if (senha.isEmpty()){
            showMenssage("Digite a senha", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        URL url = new URL("http://localhost:8080/usuarios/adm");

        HttpURLConnection conn =(HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type","application/json; charset=UTF-8");

        conn.setDoOutput(true);

        String cpfOnly = txtCpf.getText().replaceAll("\\D", "");
        String json = "{\n" +
                "  \"nome\": \"" + txtNome.getText() + "\",\n" +
                "  \"email\": \"" + txtEmail.getText() + "\",\n" +
                "  \"cpf\": \"" + cpfOnly + "\",\n" +
                "  \"secretKey\": \"adujfbdbfajdbfkjdbfkjdafkjs\",\n" +
                "  \"senha\": \"" + txtSenha.getText() + "\"\n" +
                "}";

        try(OutputStream os = conn.getOutputStream()){
            os.write(json.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        // aceitar qualquer 2xx como sucesso
        if (code >= 200 && code < 300){
            showMenssage("Sucesso ao salvar! (HTTP " + code + ")", Alert.AlertType.INFORMATION);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);

        } else {
            // ler corpo de erro para depuração
            try (java.io.InputStream is = conn.getErrorStream()){
                String body = "";
                if (is != null){
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"))){
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line).append('\n');
                        body = sb.toString();
                    }
                }
                showMenssage("Erro ao salvar! HTTP " + code + "\n" + body, Alert.AlertType.ERROR);
            }
        }

        conn.disconnect();

    }
}