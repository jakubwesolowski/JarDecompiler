package gui;

import jar.JarClass;
import jar.JarConstructor;
import jar.JarExplorer;
import jar.JarField;
import jar.JarMethod;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class Explorer extends Application {

  private static TreeItem<JarClass> rootTreeItem;
  private static TreeView<JarClass> treeFileView;
  private static FileChooser fileChooser;
  private static ListView<JarMethod> methodListView;
  private static ListView<JarField> fieldListView;
  private static ListView<JarConstructor> constructorListView;
  private static JarExplorer jarExplorer;
  private static List<JarClass> jarClasses;

  @Override
  public void start(Stage primaryStage) {

    Pane root = createRoot();
    Scene scene = new Scene(root, 960, 960);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private static Pane createRoot() {

    VBox root = new VBox();
    HBox hBox = new HBox();
    MenuBar menu = createMenu();

    treeFileView = createFileTree();
    hBox.getChildren().add(treeFileView);

    methodListView = createMethodListView();
    HBox.setHgrow(methodListView, Priority.ALWAYS);
    hBox.getChildren().add(methodListView);

    fieldListView = createFieldListView();
    HBox.setHgrow(fieldListView, Priority.ALWAYS);
    hBox.getChildren().add(fieldListView);

    constructorListView = createConstructorListView();
    HBox.setHgrow(constructorListView, Priority.ALWAYS);
    hBox.getChildren().add(constructorListView);

    VBox.setVgrow(hBox, Priority.ALWAYS);

    root.getChildren().add(menu);
    root.getChildren().add(hBox);

    createFileChooser();

    return root;
  }

  public static MenuBar createMenu() {
    MenuBar menuBar = new MenuBar();

    Menu fileMenu = new Menu("File");
    MenuItem openFile = new MenuItem("Open");
    MenuItem saveJar = new Menu("Save");
    openFile.setOnAction(e -> {
      addFileTree();
    });
    saveJar.setOnAction(e -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Open jar file");
      chooser.getExtensionFilters().add(
          new ExtensionFilter("Jar file", "*.jar")
      );

      File selectedFile = chooser.showSaveDialog(null);

      try {
        jarExplorer.saveJar(selectedFile.getAbsolutePath());

      } catch (IOException e1) {
        e1.printStackTrace();
      }

    });
    fileMenu.getItems().add(openFile);
    fileMenu.getItems().add(saveJar);
    menuBar.getMenus().add(fileMenu);

    Menu editMenu = new Menu("Edit");
    menuBar.getMenus().add(editMenu);

    Menu addMenu = new Menu("Add");
    MenuItem addMethod = new MenuItem("Add method");
    MenuItem addField = new MenuItem("Add field");
    MenuItem addConstructor = new MenuItem("Add constructor");

    addMethod.setOnAction(event -> {

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);

      Pane methodPane = new Pane();

      VBox vBox = new VBox();
      vBox.setSpacing(10);

      Label label = new Label("Select class");
      ComboBox<JarClass> comboBox = new ComboBox<>();
      TextArea codeArea = new TextArea();
      Button button = new Button("Add");

      button.setOnAction(e1 -> {

        String body = codeArea.getText();
        try {
          JarClass item = comboBox.getSelectionModel().getSelectedItem();
          CtMethod m = CtNewMethod.make(body, item.getCtClass());
          item.getCtClass().addMethod(m);
          jarExplorer.saveClass(item.getCtClass());
          item.getCtClass().defrost();
          stage.close();

        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
          e.printStackTrace();
        }
      });

      comboBox.getItems().addAll(jarClasses);

      HBox.setHgrow(label, Priority.ALWAYS);
      vBox.getChildren().add(label);
      HBox.setHgrow(comboBox, Priority.ALWAYS);
      vBox.getChildren().add(comboBox);
      HBox.setHgrow(codeArea, Priority.ALWAYS);
      vBox.getChildren().add(codeArea);
      HBox.setHgrow(button, Priority.ALWAYS);
      vBox.getChildren().add(button);
      methodPane.getChildren().add(vBox);

      Scene scene = new Scene(methodPane);

      stage.setScene(scene);
      stage.show();
    });
    addField.setOnAction(event -> {

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);

      Pane methodPane = new Pane();

      VBox vBox = new VBox();
      vBox.setSpacing(10);

      Label label = new Label("Select class");
      ComboBox<JarClass> comboBox = new ComboBox<>();
      TextArea codeArea = new TextArea();
      Button button = new Button("Add");

      button.setOnAction(e1 -> {

        String body = codeArea.getText();
        try {
          JarClass item = comboBox.getSelectionModel().getSelectedItem();
          CtField field = CtField.make(body, item.getCtClass());
          item.getCtClass().addField(field);
          jarExplorer.saveClass(item.getCtClass());
          item.getCtClass().defrost();
          stage.close();

        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
          e.printStackTrace();
        }
      });

      comboBox.getItems().addAll(jarClasses);

      HBox.setHgrow(label, Priority.ALWAYS);
      vBox.getChildren().add(label);
      HBox.setHgrow(comboBox, Priority.ALWAYS);
      vBox.getChildren().add(comboBox);
      HBox.setHgrow(codeArea, Priority.ALWAYS);
      vBox.getChildren().add(codeArea);
      HBox.setHgrow(button, Priority.ALWAYS);
      vBox.getChildren().add(button);
      methodPane.getChildren().add(vBox);

      Scene scene = new Scene(methodPane);

      stage.setScene(scene);
      stage.show();

    });
    addConstructor.setOnAction(event -> {

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);

      Pane methodPane = new Pane();

      VBox vBox = new VBox();
      vBox.setSpacing(10);

      Label label = new Label("Select class");
      ComboBox<JarClass> comboBox = new ComboBox<>();
      TextArea codeArea = new TextArea();
      Button button = new Button("Add");

      button.setOnAction(e1 -> {

        String body = codeArea.getText();
        try {
          JarClass item = comboBox.getSelectionModel().getSelectedItem();
          CtConstructor constructor = CtNewConstructor.make(body, item.getCtClass());
          item.getCtClass().addConstructor(constructor);
          jarExplorer.saveClass(item.getCtClass());
          item.getCtClass().defrost();
          stage.close();

        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
          e.printStackTrace();
        }
      });

      comboBox.getItems().addAll(jarClasses);

      HBox.setHgrow(label, Priority.ALWAYS);
      vBox.getChildren().add(label);
      HBox.setHgrow(comboBox, Priority.ALWAYS);
      vBox.getChildren().add(comboBox);
      HBox.setHgrow(codeArea, Priority.ALWAYS);
      vBox.getChildren().add(codeArea);
      HBox.setHgrow(button, Priority.ALWAYS);
      vBox.getChildren().add(button);
      methodPane.getChildren().add(vBox);

      Scene scene = new Scene(methodPane);

      stage.setScene(scene);
      stage.show();

    });

    addMenu.getItems().addAll(addField, addConstructor, addMethod);
    menuBar.getMenus().add(addMenu);

    return menuBar;
  }

  public static TreeView<JarClass> createFileTree() {
    rootTreeItem = new TreeItem<>();
    rootTreeItem.setExpanded(true);
    TreeView<JarClass> treeView = new TreeView<>(rootTreeItem);

    treeView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          JarClass selectedItem = newValue.getValue();
          addDataToListViews(Arrays.asList(selectedItem.getCtClass()));
        });

    return treeView;
  }


  public static ListView<JarMethod> createMethodListView() {
    methodListView = new ListView<>();

    methodListView.setCellFactory(lv -> {

      ListCell<JarMethod> cell = new ListCell<JarMethod>() {
        @Override
        protected void updateItem(JarMethod item, boolean empty) {
          super.updateItem(item, empty);
          textProperty().unbind();
          if (item != null) {
            textProperty().bind(new StringBinding() {
              @Override
              protected String computeValue() {
                return item.toString();
              }
            });
          } else {
            setText(null);
          }
        }
      };

      ContextMenu contextMenu = new ContextMenu();

      MenuItem editItem = new MenuItem();
      editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
      editItem.setOnAction(event -> {
        JarMethod item = cell.getItem();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Pane methodPane = new Pane();

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        TextArea methodBody = new TextArea();
        VBox.setVgrow(methodBody, Priority.ALWAYS);
        vBox.getChildren().add(methodBody);

        HBox hBox = new HBox();
        hBox.setSpacing(10);

        Button b1 = new Button("Set body");
        Button b2 = new Button("Insert after");
        Button b3 = new Button("Insert before");

        b1.setOnAction(event1 -> {
          String body = methodBody.getText();
          try {
            item.getCtMethod().setBody(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        b2.setOnAction(event1 -> {
          String body = methodBody.getText();
          System.out.println(body);

          try {
            item.getCtMethod().insertAfter(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        b3.setOnAction(event1 -> {
          String body = methodBody.getText();

          try {
            item.getCtMethod().insertBefore(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        HBox.setHgrow(b1, Priority.ALWAYS);
        HBox.setHgrow(b2, Priority.ALWAYS);
        HBox.setHgrow(b3, Priority.ALWAYS);
        hBox.getChildren().addAll(b1, b2, b3);

        VBox.setVgrow(hBox, Priority.ALWAYS);
        vBox.getChildren().add(hBox);

        methodPane.getChildren().add(vBox);
        Scene scene = new Scene(methodPane);
        stage.setScene(scene);
        stage.show();
      });

      MenuItem deleteItem = new MenuItem();
      deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
      deleteItem.setOnAction(event -> {
        JarMethod value = cell.getItem();

        try {
          value.getCtClass().removeMethod(value.getCtMethod());
          jarExplorer.saveClass(value.getCtClass());
          value.getCtClass().defrost();
          methodListView.getItems().remove(cell.getItem());

        } catch (NotFoundException e) {
          e.printStackTrace();
        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
          e.printStackTrace();
        }
      });

      contextMenu.getItems().addAll(editItem, deleteItem);
      cell.textProperty().bind(cell.itemProperty().asString());

      cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
        if (isNowEmpty) {
          cell.setContextMenu(null);
        } else {
          cell.setContextMenu(contextMenu);
        }
      });
      return cell;
    });

    return methodListView;
  }

  public static ListView<JarField> createFieldListView() {
    fieldListView = new ListView<>();

    fieldListView.setCellFactory(lv -> {

      ListCell<JarField> cell = new ListCell<JarField>() {
        @Override
        protected void updateItem(JarField item, boolean empty) {
          super.updateItem(item, empty);
          textProperty().unbind();
          if (item != null) {
            textProperty().bind(new StringBinding() {
              @Override
              protected String computeValue() {
                return item.toString();
              }
            });
          } else {
            setText(null);
          }
        }
      };
      ContextMenu contextMenu = new ContextMenu();

      MenuItem deleteItem = new MenuItem();
      deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
      deleteItem.setOnAction(event -> {
        JarField value = cell.getItem();

        try {
          System.out.println(value.getCtField().getName());
          value.getCtClass().removeField(value.getCtField());
          jarExplorer.saveClass(value.getCtClass());
          value.getCtClass().defrost();
          fieldListView.getItems().remove(cell.getItem());

        } catch (NotFoundException e) {
          e.printStackTrace();
        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
//          e.printStackTrace();
        }
      });

      contextMenu.getItems().addAll(deleteItem);

      cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
        if (isNowEmpty) {
          cell.setContextMenu(null);
        } else {
          cell.setContextMenu(contextMenu);
        }
      });
      return cell;
    });

    return fieldListView;
  }

  private static ListView<JarConstructor> createConstructorListView() {
    constructorListView = new ListView<>();

    constructorListView.setCellFactory(lv -> {

      ListCell<JarConstructor> cell = new ListCell<JarConstructor>() {
        @Override
        protected void updateItem(JarConstructor item, boolean empty) {
          super.updateItem(item, empty);
          textProperty().unbind();
          if (item != null) {
            textProperty().bind(new StringBinding() {
              @Override
              protected String computeValue() {
                return item.toString();
              }
            });
          } else {
            setText(null);
          }
        }
      };
      ContextMenu contextMenu = new ContextMenu();

      MenuItem editItem = new MenuItem();
      editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
      editItem.setOnAction(event -> {

        JarConstructor item = cell.getItem();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Pane methodPane = new Pane();

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        TextArea methodBody = new TextArea();
        VBox.setVgrow(methodBody, Priority.ALWAYS);
        vBox.getChildren().add(methodBody);

        HBox hBox = new HBox();
        hBox.setSpacing(10);

        Button b1 = new Button("Set body");
        Button b2 = new Button("Insert after");
        Button b3 = new Button("Insert before");

        b1.setOnAction(event1 -> {
          String body = methodBody.getText();
          try {
            item.getCtConstructor().setBody(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        b2.setOnAction(event1 -> {
          String body = methodBody.getText();

          try {
            item.getCtConstructor().insertAfter(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        b3.setOnAction(event1 -> {
          String body = methodBody.getText();

          try {
            item.getCtConstructor().insertAfter(body);
            jarExplorer.saveClass(item.getCtClass());
            item.getCtClass().defrost();
            stage.close();

          } catch (CannotCompileException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Compile error");
            a.setHeaderText(null);
            a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
            a.showAndWait();
            e.printStackTrace();
          }
        });

        HBox.setHgrow(b1, Priority.ALWAYS);
        HBox.setHgrow(b2, Priority.ALWAYS);
        HBox.setHgrow(b3, Priority.ALWAYS);
        hBox.getChildren().addAll(b1, b2, b3);

        VBox.setVgrow(hBox, Priority.ALWAYS);
        vBox.getChildren().add(hBox);

        methodPane.getChildren().add(vBox);
        Scene scene = new Scene(methodPane);
        stage.setScene(scene);
        stage.show();
      });

      MenuItem deleteItem = new MenuItem();
      deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
      deleteItem.setOnAction(event -> {
        JarConstructor value = cell.getItem();

        try {
          value.getCtClass().removeConstructor(value.getCtConstructor());
          jarExplorer.saveClass(value.getCtClass());
          value.getCtClass().defrost();
          constructorListView.getItems().remove(cell.getItem());

        } catch (NotFoundException e) {
          e.printStackTrace();
        } catch (CannotCompileException e) {
          Alert a = new Alert(AlertType.ERROR);
          a.setTitle("Compile error");
          a.setHeaderText(null);
          a.setContentText("Cannot compile code " + "\n" + e.getCause().toString());
          a.showAndWait();
          e.printStackTrace();
        }
      });

      contextMenu.getItems().addAll(editItem, deleteItem);

      cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
        if (isNowEmpty) {
          cell.setContextMenu(null);
        } else {
          cell.setContextMenu(contextMenu);
        }
      });
      return cell;
    });

    return constructorListView;
  }

  private static void addFileTree() {

    File selectedFile = fileChooser.showOpenDialog(null);

    if (selectedFile != null) {
      String selectedJarPath = selectedFile.getAbsolutePath();

      try {
        jarExplorer = new JarExplorer(selectedFile.getAbsolutePath());
        jarClasses = jarExplorer.getJarClasses();

        for (JarClass clazz : jarClasses) {
          rootTreeItem.getChildren().add(new TreeItem<>(clazz));
        }

      } catch (NotFoundException e) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle("Exception");
        a.setHeaderText(null);
        a.setContentText(e.getCause().toString());
        a.showAndWait();
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void createFileChooser() {
    fileChooser = new FileChooser();
    fileChooser.setTitle("Open jar file");
    fileChooser.getExtensionFilters().add(
        new ExtensionFilter("Jar file", "*.jar")
    );
  }

  private static void addDataToListViews(List<CtClass> ctClasses) {
    methodListView.getItems().clear();
    fieldListView.getItems().clear();
    constructorListView.getItems().clear();

    for (CtClass ctClass : ctClasses) {
      CtMethod[] methods = ctClass.getDeclaredMethods();
      CtField[] fields = ctClass.getFields();
      CtConstructor[] constructors = ctClass.getConstructors();

      for (CtMethod method : methods) {
        methodListView.getItems().add(new JarMethod(ctClass, method));
      }

      for (CtField field : fields) {
        fieldListView.getItems().add(new JarField(ctClass, field));
      }

      for (CtConstructor constructor : constructors) {
        constructorListView.getItems().add(new JarConstructor(ctClass, constructor));
      }
    }
  }
}
