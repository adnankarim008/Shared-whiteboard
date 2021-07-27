package org.example;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class ClientController implements OnServerDrawEvent {
    @FXML
    public ToggleButton btnLine ;

    @FXML
    public ToggleButton btnRec ;

    @FXML
    public ToggleButton btnCircle ;

    @FXML
    public ColorPicker btnColor ;


    @FXML
    public AnchorPane pane;

    @FXML
    public TextField sendText;

    @FXML
    public Button btnSendText;

    @FXML
    public TextFlow chatHistory;

    @FXML
    public ListView<String> peerList;

    private GraphicsContext gc;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private Line tempLine;
    private Rectangle tempRec;
    private Ellipse tempCircle;

    public ArrayList<Shape> shapeHistory;
    public ArrayList<CommandWrapper> drawHistory;

    public SocketHandler socketHandler;
    public static Gson gson = new Gson();

    @FXML
    void initialize() {
        shapeHistory = new ArrayList<Shape>();
        drawHistory = new ArrayList<>();
        //gc = canvas.getGraphicsContext2D();
        //gc.setStroke(Color.RED);
        btnColor.setValue(Color.RED);

        btnLine.setSelected(true);

        btnLine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnCircle.setSelected(false);
                btnRec.setSelected(false);
            }
        });

        btnCircle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnLine.setSelected(false);
                btnRec.setSelected(false);
            }
        });

        btnRec.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnLine.setSelected(false);
                btnCircle.setSelected(false);
            }
        });
        btnSendText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    var toSend = new CommandWrapper("message", sendText.getText(), socketHandler.username);
                    socketHandler.out.write(gson.toJson(toSend) + "\n");
                    socketHandler.out.flush();
                    sendText.setText("");
                    chatReceived(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        pane.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                //System.out.println("Mouse Down");

                x1 = event.getX();
                y1 = event.getY();


                if(btnLine.isSelected()){
                    tempLine = new Line(x1, y1, x1, y1);
                    tempLine.setStroke(btnColor.getValue());
                    pane.getChildren().add(tempLine);
                }
                else if(btnRec.isSelected()){
                    tempRec =new Rectangle();
                    tempRec.setX(x1);
                    tempRec.setY(y1);

                    tempRec.setStroke(btnColor.getValue());
                    tempRec.setFill(Color.TRANSPARENT);
                    pane.getChildren().add(tempRec);
                }
                else if(btnCircle.isSelected()){
                    tempCircle = new Ellipse();
                    tempCircle.setCenterX(x1);
                    tempCircle.setCenterY(y1);
                    tempCircle.setRadiusX(0);
                    tempCircle.setRadiusY(0);

                    tempCircle.setStroke(btnColor.getValue());
                    tempCircle.setFill(Color.TRANSPARENT);
                    pane.getChildren().add(tempCircle);
                }




            }
        });
        pane.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                //System.out.println("Mouse Drag");

                x2 = event.getX();
                y2 = event.getY();

                //drawRectangle(x1,y1,x2-x1, y2-y1);

                if(btnLine.isSelected()){
                    tempLine.setEndX(x2);
                    tempLine.setEndY(y2);
                }
                else if(btnRec.isSelected()){
                    tempRec.setWidth(x2 - tempRec.getX());
                    tempRec.setHeight(y2 - tempRec.getY());
                }
                else if(btnCircle.isSelected()){
                    tempCircle.setCenterX(x1 + ((x2-x1)/2));
                    tempCircle.setCenterY(y1 + ((y2-y1)/2));
                    tempCircle.setRadiusX((x2 - x1)/2);
                    tempCircle.setRadiusY((y2 - y1)/2);
                }
            }
        });
        pane.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                //System.out.println("Mouse Up");

                x2 = event.getX();
                y2 = event.getY();

                var obj = new CommandWrapper();

                obj.x1 = x1;
                obj.x2 = x2;
                obj.y1 = y1;
                obj.y2 = y2;
                var color = btnColor.getValue();
                obj.colorRed = (int)(255*color.getRed());
                obj.colorGreen = (int)(255*color.getGreen());
                obj.colorBlue = (int)(255*color.getBlue());


                if(btnLine.isSelected()){
                    shapeHistory.add(tempLine);
                    obj.Type = "line";
                }
                else if(btnRec.isSelected()){
                    shapeHistory.add(tempRec);
                    obj.Type = "rec";
                }
                else if(btnCircle.isSelected()){
                    shapeHistory.add(tempCircle);
                    obj.Type = "circle";
                }

                drawHistory.add(obj);

                try {
                    socketHandler.sendServer(obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        var port = 0;
        TextInputDialog dialog2 = new TextInputDialog("10001");
        dialog2.setTitle("Port");
        dialog2.setHeaderText("Enter port to connect to");
        Optional<String> result2 = dialog2.showAndWait();

        result2.ifPresentOrElse(p -> {

        }, () -> System.exit(1));

        TextInputDialog dialog = new TextInputDialog(null);
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        Optional<String> result = dialog.showAndWait();

        result.ifPresentOrElse(name -> {
            socketHandler = new SocketHandler(this, name, Integer.valueOf(result2.get()));
            new Thread(socketHandler).start();
        }, () -> System.exit(1));



    }

    @Override
    public void drawLine(CommandWrapper data) {
        tempLine = new Line(data.x1, data.y1, data.x2, data.y2);

        var color = Color.rgb(data.colorRed,data.colorGreen,data.colorBlue);
        tempLine.setStroke(color);
        pane.getChildren().add(tempLine);
    }

    @Override
    public void drawRect(CommandWrapper data) {
        tempRec =new Rectangle();
        tempRec.setX(data.x1);
        tempRec.setY(data.y1);
        tempRec.setWidth(data.x2 - tempRec.getX());
        tempRec.setHeight(data.y2 - tempRec.getY());
        var color = Color.rgb(data.colorRed,data.colorGreen,data.colorBlue);
        tempRec.setStroke(color);
        tempRec.setFill(Color.TRANSPARENT);
        pane.getChildren().add(tempRec);
    }

    @Override
    public void drawCircle(CommandWrapper data) {
        tempCircle = new Ellipse();
        tempCircle.setCenterX(data.x1 + ((data.x2-data.x1)/2));
        tempCircle.setCenterY(data.y1 + ((data.y2-data.y1)/2));
        tempCircle.setRadiusX((data.x2 - data.x1)/2);
        tempCircle.setRadiusY((data.y2 - data.y1)/2);

        var color = Color.rgb(data.colorRed,data.colorGreen,data.colorBlue);
        tempCircle.setStroke(color);
        tempCircle.setFill(Color.TRANSPARENT);
        pane.getChildren().add(tempCircle);
    }
    @Override
    public void chatReceived(CommandWrapper data){
        Text text1 = new Text(data.from + ": ");
        text1.setStyle("-fx-font-weight: bold");
        Text text2 = new Text(data.message + "\n");
        chatHistory.getChildren().addAll(text1, text2);
    }
    @Override
    public void resetBoard(){
        shapeHistory = new ArrayList<>();
        pane.getChildren().clear();
        drawHistory = new ArrayList<>();
    }
    @Override
    public void peerConnected(String username){
        if(!socketHandler.peers.stream().anyMatch(x-> x.equals(username))){
            socketHandler.peers.add(username);
        }

        peerList.setItems(socketHandler.peers);
    }
    @Override
    public void peerDisconnected(String username){
        socketHandler.peers.removeIf(x-> x.equals(username));
        peerList.setItems(socketHandler.peers);
    }



    public static class SocketHandler implements Runnable {
        public BufferedReader in;
        public BufferedWriter out;
        public static Gson gson = new Gson();
        public Socket socket;
        public ObservableList<String> peers;

        private OnServerDrawEvent mListener;
        private String username;
        private int port;

        public SocketHandler(OnServerDrawEvent mListener, String username, int port){
            this.mListener = mListener;
            this.username = username;
            this.port = port;
        }

        public void run(){
            try {
                setUpClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setUpClient() throws IOException {
            var address = "localhost";

            try{
                socket = new Socket(address, port);
            }catch(Exception e){
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error while connecting");
                    alert.setContentText("Port " + port);

                    alert.showAndWait();
                    System.exit(1);
                });
                return;
            }

            peers = FXCollections.observableArrayList();

            // Get the input/output streams for reading/writing data from/to the socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            Scanner scanner = new Scanner(System.in);
            String inputStr = null;

            var toSend = new CommandWrapper("username", this.username);
            out.write(gson.toJson(toSend) + "\n");
            out.flush();

            //While the user input differs from "exit"
            while (inputStr == null)
            {

                // Send the input string to the server by writing to the socket output stream
                //out.write(inputStr + "\n");
                //out.flush();
                //System.out.println("Message sent");

                // Receive the reply from the server by reading from the socket input stream
                String received = in.readLine(); // This method blocks until there  is something to read from the
                // input stream
                System.out.println("Message received: " + received);

                try{
                    if(received == null)
                        continue;

                    CommandWrapper data = gson.fromJson(received, CommandWrapper.class);
                    if(data.Type.equals("error")){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Error from server");
                            alert.setContentText(data.message);

                            alert.showAndWait();
                            System.exit(1);
                        });
                    }

                    if(data.Type.equals("line")){
                        Platform.runLater(() -> {
                            mListener.drawLine(data);
                        });

                    }
                    else if(data.Type.equals("rec")){
                        Platform.runLater(() -> {
                            mListener.drawRect(data);
                        });
                    }
                    else if(data.Type.equals("circle")){
                        Platform.runLater(() -> {
                            mListener.drawCircle(data);
                        });
                    }
                    else if(data.Type.equals("message")){
                        Platform.runLater(() -> {
                            mListener.chatReceived(data);
                        });
                    }
                    else if(data.Type.equals("reset")){
                        Platform.runLater(() -> {
                            mListener.resetBoard();
                        });
                    }
                    else if(data.Type.equals("peerconnect")){
                        Platform.runLater(() -> {
                            mListener.peerConnected(data.message);
                        });
                    }
                    else if(data.Type.equals("peerdisconnect")){
                        Platform.runLater(() -> {
                            mListener.peerDisconnected(data.message);
                        });
                    }

                }
                catch(Exception e){
                    System.out.println(e);
                }
            }

            scanner.close();
        }

        public void sendServer(CommandWrapper drawDetail) throws IOException {
            out.write(gson.toJson(drawDetail) + "\n");
            out.flush();
        }


    }

    public void shutdown() throws IOException {
        // cleanup code here...
        System.out.println("Stop");
        socketHandler.socket.close();
        socketHandler.in.close();
        socketHandler.out.close();
    }

}

interface OnServerDrawEvent{
    void drawLine(CommandWrapper data);
    void drawCircle(CommandWrapper data);
    void drawRect(CommandWrapper data);
    void chatReceived(CommandWrapper data);
    void peerConnected(String username);
    void peerDisconnected(String username);
    void resetBoard();
}
