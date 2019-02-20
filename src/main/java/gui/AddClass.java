package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@SuppressWarnings("ALL")
public class AddClass {

  public static String[] addClass() {
    Dialog dialog = new Dialog();
    dialog.setTitle("Add class.");
    dialog.setHeaderText(null);

    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
//    stage.getIcons().add(new Image("badger.png"));

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 250, 10, 10));

    ChoiceBox accessLevel = new ChoiceBox(FXCollections.observableArrayList(
        "public", "protected", "private"));
    accessLevel.getSelectionModel().selectFirst();

    ChoiceBox staticFinal = new ChoiceBox(FXCollections.observableArrayList(
        "", "abstract", "final"));
    staticFinal.getSelectionModel().selectFirst();

    ChoiceBox cI = new ChoiceBox(FXCollections.observableArrayList(
        "class", "interface"));
    cI.getSelectionModel().selectFirst();

    TextField fullName = new TextField();
    fullName.setPromptText("pl.edu.wat.wcy.jfk.myClass");


    grid.add(new Label("Access level: "), 0, 0);
    grid.add(accessLevel, 1, 0);
    grid.add(new Label("Modifier:"), 0, 1);
    grid.add(staticFinal, 1, 1);
    grid.add(new Label("Type:"), 0, 2);
    grid.add(cI, 1, 2);
    grid.add(new Label("Full name:"), 0, 3);
    grid.add(fullName, 1, 3);

    ButtonType applyButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);

    dialog.getDialogPane().setContent(grid);
    String res[] = new String[4];
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == applyButton) {

        res[0] = accessLevel.getSelectionModel().getSelectedItem().toString();
        res[1] = staticFinal.getSelectionModel().getSelectedItem().toString();
        res[2] = cI.getSelectionModel().getSelectedItem().toString();
        res[3] = fullName.getText();
        return null;
      }
      return null;
    });
    dialog.showAndWait();
    return res;
  }
}

