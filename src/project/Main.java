package project;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		//ImagePlus originalImagePlus = new ImagePlus("file:///d:/lena_std.jpg");
		//new Process(originalImagePlus);
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
/*
 * Byl vytvoøen nový projekt, do nìj se budou postupnì pøesouvat potøebné funkce
 * dnešní úkol: pøenést metody na pøevedení obrázkù z RGB do YCbCr, zobrazit dva
 * obrázky, udìlat jejich rozdíl, zobrazit v RGB rozdílový snímek - pouze jeho
 * jasovou složku, pøi zobrazení použít vzorec x = ( (s2-s1) + 255 ) / 2
 * uložit rozdílové snímky normálnì v rozsahu -255 až 255, zobrazovat v
 * rozsahu 0 až 255
 */
