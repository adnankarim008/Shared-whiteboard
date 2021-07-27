package org.example;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.stage.FileChooser;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerController implements OnClientDrawEvent {
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
    public ListView<ClientHandler> peerList;

    @FXML
    public MenuItem btnFileNew;
    @FXML
    public MenuItem btnFileOpen;
    @FXML
    public MenuItem btnFileSave;



    private GraphicsContext gc;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private Line tempLine;
    private Rectangle tempRec;
    private Ellipse tempCircle;

    public ArrayList<Shape> shapeHistory;

    public ServerHandler serverHandler;
    public static Gson gson = new Gson();
    @FXML
    void initialize() {
        shapeHistory = new ArrayList<>();

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
                var toSend = new CommandWrapper("message", sendText.getText(), "Host");
                relayAll(toSend, 0);
                sendText.setText("");
                chatReceived(toSend);
            }
        });
        btnFileNew.setOnAction(e-> {
            shapeHistory = new ArrayList<>();
            pane.getChildren().clear();
            serverHandler.drawHistory = new ArrayList<>();

            relayAll(new CommandWrapper("reset","reset"), 0);
        });
        btnFileOpen.setOnAction(e-> {
            shapeHistory = new ArrayList<>();
            pane.getChildren().clear();
            serverHandler.drawHistory = new ArrayList<>();

            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Whiteboard File (*.wft)", "*.wft");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showOpenDialog(((MenuItem)e.getTarget()).getParentPopup().getOwnerWindow());

            if (file != null) {
                try {
                    var lines = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    var drawHistoryx = new ArrayList<CommandWrapper>(Arrays.asList(gson.fromJson(lines, CommandWrapper[].class)));

                    relayAll(new CommandWrapper("reset","reset"), 0);
                    for(var data : drawHistoryx){
                        ApplyCommand(data);
                        relayAll(data, 0);
                    }
                } catch (IOException ex) {

                }
            }
        });
        btnFileSave.setOnAction(e-> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Whiteboard File (*.wft)", "*.wft");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(((MenuItem)e.getTarget()).getParentPopup().getOwnerWindow());

            if (file != null) {
                try {
                    PrintWriter writer;
                    writer = new PrintWriter(file);
                    writer.println(gson.toJson(serverHandler.drawHistory));
                    writer.close();
                } catch (IOException ex) {

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
                serverHandler.drawHistory.add(obj);

                serverHandler.sendAll(obj);
            }
        });


        //serverHandler = new ServerHandler(this, 10001);
        //new Thread(serverHandler).start();

        TextInputDialog dialog2 = new TextInputDialog("10001");
        dialog2.setTitle("Port");
        dialog2.setHeaderText("Enter port for server");
        Optional<String> result2 = dialog2.showAndWait();

        result2.ifPresentOrElse(p -> {
            serverHandler = new ServerHandler(this, Integer.valueOf(p));
            new Thread(serverHandler).start();
        }, () -> System.exit(1));

        peerList.setItems(serverHandler.clients);
        peerList.setCellFactory(lv -> {

            ListCell<ClientHandler> cell = new ListCell<>();

            ContextMenu contextMenu = new ContextMenu();


            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Kick \"%s\"", cell.itemProperty()));
            editItem.setOnAction(event -> {
                ClientHandler item = cell.getItem();

                item.sendError("Kicked by host");
                try {
                    item.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverHandler.clients.removeIf(x-> x.username.equals(item.username));
                relayAll(new CommandWrapper("peerdisconnect", item.username), 0);
            });

            contextMenu.getItems().addAll(editItem);

            cell.textProperty().bind(Bindings.
                    when(cell.emptyProperty()).
                    then("").
                    otherwise(Bindings.format("%s", cell.itemProperty())));

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
    }
    @Override
    public void ApplyCommand(CommandWrapper data){
        if(data.Type.equals("line")){
            Platform.runLater(() -> {
                drawLine(data);
            });

        }
        else if(data.Type.equals("rec")){
            Platform.runLater(() -> {
                drawRect(data);
            });
        }
        else if(data.Type.equals("circle")){
            Platform.runLater(() -> {
                drawCircle(data);
            });
        }
        else if(data.Type.equals("message")){
            Platform.runLater(() -> {
                chatReceived(data);
            });
        }
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
    public void relayAll(CommandWrapper data, int senderClient){
        serverHandler.drawHistory.add(data);
        for(var client : serverHandler.clients){
            if(client.id == senderClient)
                continue;

            client.out.write(gson.toJson(data) + "\n");
            client.out.flush();
        }
    }
    @Override
    public boolean checkUsername(ClientHandler client){
        if(serverHandler.clients.stream().anyMatch(x-> x.username.equals(client.username))){
            return false;
        }
        Platform.runLater(() -> {
            serverHandler.clients.add(client);
            peerList.setItems(serverHandler.clients);
        });

        return true;
    }
    @Override
    public void clientconnect(String username){
        relayAll(new CommandWrapper("peerconnect", username), 0);
    }
    @Override
    public void clientDisconnect(String username){
        Platform.runLater(() -> {
            serverHandler.clients.removeIf(x-> x.username == username);
        });
        relayAll(new CommandWrapper("peerdisconnect", username), 0);
    }
    @Override
    public List<String> sendAllUsers(){
        return serverHandler.clients.stream().map(x-> x.username).collect(Collectors.toList());
    }

    private static class ServerHandler implements Runnable {
        public static Gson gson = new Gson();
        public ObservableList<ClientHandler> clients;
        public ArrayList<CommandWrapper> drawHistory;
        public ServerSocket listeningSocket;
        private OnClientDrawEvent mListener;
        private int port;

        public ServerHandler(OnClientDrawEvent mListener, int port){
            this.mListener = mListener;
            this.port = port;
        }

        public void setUpServer(){
            drawHistory = new ArrayList<>();
            clients = FXCollections.observableArrayList();
            listeningSocket = null;
            Socket clientSocket = null;

            //var port = 9930;
            try {
                //Create a server socket listening on port 4444
                listeningSocket = new ServerSocket(port);
                listeningSocket.setReuseAddress(true);
                int i = 1; //counter to keep track of the number of clients


                //Listen for incoming connections for ever
                while (true)
                {
                    System.out.println("Server listening on port "+ port + " for connections");
                    clientSocket = listeningSocket.accept(); //This method will block until a connection request is received
                    i++;
                    System.out.println("Client conection number " + i + " accepted:");
                    ClientHandler clientSock = new ClientHandler(clientSocket, i, drawHistory, mListener);
                    //clients.add(clientSock);
                    new Thread(clientSock).start();
                }
            }
            catch (BindException e){
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Port in use");
                    alert.setContentText("Port already in use - " + this.port);

                    alert.showAndWait();
                    System.exit(1);
                });
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (listeningSocket != null) {
                    try {
                        listeningSocket.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void run(){
            setUpServer();
        }

        public void sendAll(CommandWrapper drawDetail){
            for(var client : clients){
                client.out.write(gson.toJson(drawDetail) + "\n");
                client.out.flush();
            }
        }

    }

    public void shutdown() throws IOException {
        // cleanup code here...
        System.out.println("Stop");
        serverHandler.listeningSocket.close();
    }

}

class ClientHandler implements Runnable {
    public static Gson gson = new Gson();
    private final Socket clientSocket;
    public final int id;
    public String username;
    public PrintWriter out;
    public BufferedReader in;
    private ArrayList<CommandWrapper> serverHistory ;
    private OnClientDrawEvent mListener;

    // Constructor
    public ClientHandler(Socket socket, int id, ArrayList<CommandWrapper> serverHistory, OnClientDrawEvent mListener)
    {
        this.clientSocket = socket;
        this.id = id;
        this.serverHistory = serverHistory;
        this.mListener = mListener;
    }

    public void run()
    {
        out = null;
        in = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            var user = in.readLine();
            var userData = gson.fromJson(user, CommandWrapper.class);
            if(!userData.Type.equals("username")){
                this.sendError("Unknown error");
                this.close();
                return;
            }
            this.username = userData.message;
            if(!mListener.checkUsername(this)){
                this.sendError("Username already exists");
                this.close();
                return;
            }


            out.write(gson.toJson(new CommandWrapper("username", "confirm")) + "\n");
            out.flush();

            for(var history : serverHistory){
                out.write(gson.toJson(history) + "\n");
                out.flush();
            }

            for(var peer : mListener.sendAllUsers()){
                out.write(gson.toJson(new CommandWrapper("peerconnect", peer)) + "\n");
                out.flush();
            }

            mListener.clientconnect(username);

            while ((line = in.readLine()) != null) {

                System.out.println("Client " + id + " sent - " + line);
                CommandWrapper data = gson.fromJson(line, CommandWrapper.class);

                mListener.ApplyCommand(data);

                mListener.relayAll(data, this.id);
            }

            mListener.clientDisconnect(this.username);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendError(String message){
        out.write(gson.toJson(new CommandWrapper("error", message)) + "\n");
        out.flush();
    }

    public void close() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
        return;
    }

    @Override
    public String toString() {
        return this.username;
    }
}

interface OnClientDrawEvent{
    void ApplyCommand(CommandWrapper data);
    void drawLine(CommandWrapper data);
    void drawCircle(CommandWrapper data);
    void drawRect(CommandWrapper data);
    void chatReceived(CommandWrapper data);
    void relayAll(CommandWrapper data, int senderClient);
    boolean checkUsername(ClientHandler client);
    void clientconnect(String username);
    void clientDisconnect(String username);
    List<String> sendAllUsers();
}
