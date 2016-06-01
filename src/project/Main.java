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
 * Byl vytvo�en nov� projekt, do n�j se budou postupn� p�esouvat pot�ebn� funkce
 * dne�n� �kol: p�en�st metody na p�eveden� obr�zk� z RGB do YCbCr, zobrazit dva
 * obr�zky, ud�lat jejich rozd�l, zobrazit v RGB rozd�lov� sn�mek - pouze jeho
 * jasovou slo�ku, p�i zobrazen� pou��t vzorec x = ( (s2-s1) + 255 ) / 2
 * ulo�it rozd�lov� sn�mky norm�ln� v rozsahu -255 a� 255, zobrazovat v
 * rozsahu 0 a� 255
 */
