package com.talento360;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.talento360.dao.AuthDAO;
import com.talento360.dao.DashboardDAO;
import com.talento360.dao.DepartmentDAO;
import com.talento360.dao.EmployeeDAO;
import com.talento360.dao.RequestDAO;
import com.talento360.models.AdministrativeRequest;
import com.talento360.models.Employee;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainApp extends Application {
    private static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Stage stage;
    private BorderPane root;
    private StackPane appFrame;
    private Button btnDashboard;
    private Button profileButton;
    private Button departmentsButton;
    private Button vacationsButton;
    private Button disabilitiesButton;
    private Button permissionsButton;
    private Button maternityButton;
    private final Map<Integer, String> statusOverrides = new LinkedHashMap<>();

    private final AuthDAO authDAO = new AuthDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private Runnable activeRequestsRefresh = () -> {};
    private Runnable activeRequestsReset = () -> {};

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Talento 360 Humano — Gobernación de Boyacá");
        stage.setOnShown(e -> forceMaximized());
        installFullWindowGuard();
        showLogin();
        stage.show();
        forceMaximized();
    }

    private void showLogin() {
        HBox wrapper = new HBox();
        wrapper.getStyleClass().add("login-wrapper");
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane left = new StackPane();
        left.getStyleClass().add("login-left");
        left.setMinWidth(420);
        left.setPrefWidth(640);
        left.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(left, Priority.ALWAYS);
        ImageView bg = image("/assets/login_left_panel.png", 640, 820, false);
        bg.fitWidthProperty().bind(left.widthProperty());
        bg.fitHeightProperty().bind(left.heightProperty());
        bg.setPreserveRatio(false);
        bg.setSmooth(true);
        bg.setOpacity(0.90);
        left.getChildren().add(bg);

        VBox panel = new VBox(14);
        panel.getStyleClass().add("login-panel");
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setMinWidth(540);
        panel.setPrefWidth(620);
        panel.setMaxWidth(720);
        HBox.setHgrow(panel, Priority.NEVER);

        ImageView logo = image("/assets/login_logo.png", 370, 145, true);
        VBox logoBox = new VBox(logo);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 8, 0));

        Label welcome = new Label("Bienvenido(a)");
        welcome.getStyleClass().add("login-title");
        Label subtitle = new Label("Inicia sesión para acceder al sistema Talento 360 Humano.");
        subtitle.getStyleClass().add("muted");

        Label userLabel = new Label("Usuario");
        userLabel.getStyleClass().add("field-label");
        HBox userRow = fieldWithIcon("👤", "Ingresa tu usuario");
        TextField userField = (TextField) ((HBox)userRow).getChildren().get(1);
        userField.setText("admin@boyaca.gov.co");

        Label passLabel = new Label("Contraseña");
        passLabel.getStyleClass().add("field-label");
        HBox passRow = new HBox(0);
        passRow.getStyleClass().add("input");
        passRow.setAlignment(Pos.CENTER_LEFT);
        Label lockIcon = new Label("🔒 ");
        lockIcon.setStyle("-fx-text-fill: #9aaabb; -fx-font-size: 14px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingresa tu contraseña");
        passwordField.setText("admin123");
        passwordField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-font-size: 14px;");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Ingresa tu contraseña");
        visiblePasswordField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-font-size: 14px;");
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.setMaxWidth(Double.MAX_VALUE);
        visiblePasswordField.setMaxWidth(Double.MAX_VALUE);
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        passwordStack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);

        StackPane eyeIcon = new StackPane(image("/assets/icon_eye_login.png", 18, 18, true));
        eyeIcon.setStyle("-fx-cursor: hand; -fx-padding: 0 0 0 10;");
        final boolean[] passwordVisible = {false};
        eyeIcon.setOnMouseClicked(event -> {
            passwordVisible[0] = !passwordVisible[0];
            passwordField.setVisible(!passwordVisible[0]);
            passwordField.setManaged(!passwordVisible[0]);
            visiblePasswordField.setVisible(passwordVisible[0]);
            visiblePasswordField.setManaged(passwordVisible[0]);
            if (passwordVisible[0]) {
                visiblePasswordField.requestFocus();
                visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        });
        passRow.getChildren().addAll(lockIcon, passwordStack, eyeIcon);

        Label forgot = new Label("¿Olvidaste tu contraseña?");
        forgot.getStyleClass().add("forgot");
        HBox forgotRow = new HBox(forgot);
        forgotRow.setAlignment(Pos.CENTER_RIGHT);

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        Button loginButton = new Button("  Iniciar sesión");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        Label lockBtn = new Label("🔒");
        lockBtn.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        loginButton.setGraphic(lockBtn);
        loginButton.setContentDisplay(ContentDisplay.LEFT);
        loginButton.setGraphicTextGap(8);
        loginButton.setDefaultButton(true);
        Runnable loginAction = () -> {
            error.setVisible(false);
            error.setManaged(false);
            loginButton.setDisable(true);
            try {
                if (authDAO.login(userField.getText(), passwordField.getText())) {
                    showMainLayout();
                } else {
                    error.setText("⚠  Usuario, contraseña o conexión incorrecta.");
                    error.setVisible(true);
                    error.setManaged(true);
                    loginButton.setDisable(false);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                error.setText("⚠  No se pudo iniciar sesión. Revisa la conexión a PostgreSQL o las credenciales.");
                error.setVisible(true);
                error.setManaged(true);
                loginButton.setDisable(false);
            }
        };
        loginButton.setOnAction(e -> loginAction.run());
        passwordField.setOnAction(e -> loginAction.run());
        visiblePasswordField.setOnAction(e -> loginAction.run());
        userField.setOnAction(e -> passwordField.requestFocus());

        HBox goldSepRow = new HBox();
        goldSepRow.setAlignment(Pos.CENTER);
        goldSepRow.setPadding(new Insets(6, 0, 6, 0));
        Separator sep1 = new Separator();
        sep1.getStyleClass().add("gold-separator");
        HBox.setHgrow(sep1, Priority.ALWAYS);
        Region dot = new Region();
        dot.getStyleClass().add("gold-dot");
        dot.setMinSize(8, 8);
        dot.setMaxSize(8, 8);
        Separator sep2 = new Separator();
        sep2.getStyleClass().add("gold-separator");
        HBox.setHgrow(sep2, Priority.ALWAYS);
        goldSepRow.getChildren().addAll(sep1, dot, sep2);

        HBox secureBox = new HBox(10);
        secureBox.setAlignment(Pos.CENTER);
        StackPane shieldIcon = new StackPane(image("/assets/icon_shield_unique.png", 22, 22, true));
        shieldIcon.getStyleClass().add("login-asset-icon-slot");
        VBox secureTexts = new VBox(2);
        Label secureTitle = new Label("Acceso seguro");
        secureTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0c6b52;");
        Label secureSubtitle = new Label("Tus datos están protegidos");
        secureSubtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #3d7a6a;");
        secureTexts.getChildren().addAll(secureTitle, secureSubtitle);
        secureBox.getChildren().addAll(shieldIcon, secureTexts);

        panel.getChildren().addAll(logoBox, welcome, subtitle, userLabel, userRow, passLabel, passRow,
                forgotRow, loginButton, error, goldSepRow, secureBox);
        wrapper.getChildren().addAll(left, panel);

        StackPane container = new StackPane(wrapper);
        container.getStyleClass().add("login-background");
        Scene scene = new Scene(container, 1280, 820);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1180);
        stage.setMinHeight(760);
        forceMaximized();
    }

    private HBox fieldWithIcon(String iconChar, String prompt) {
        HBox row = new HBox(0);
        row.getStyleClass().add("input");
        row.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(iconChar + " ");
        icon.setStyle("-fx-text-fill: #9aaabb; -fx-font-size: 14px;");
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-font-size: 13px;");
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(icon, field);
        return row;
    }

    private void showMainLayout() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(createSidebar());
        appFrame = new StackPane(root);
        appFrame.getStyleClass().add("app-frame");
        setActive(btnDashboard);
        setDashboardView();
        Scene scene = new Scene(appFrame, 1440, 900);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(800);
        forceMaximized();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(268);
        sidebar.setPadding(new Insets(24, 0, 0, 0));

        ImageView logo = image("/assets/talento_logo_card.png", 218, 128, true);
        StackPane logoCard = new StackPane(logo);
        logoCard.getStyleClass().add("sidebar-logo-card");
        logoCard.setPrefHeight(134);
        VBox.setMargin(logoCard, new Insets(0, 16, 12, 16));

        btnDashboard = sidebarButton("/assets/sidebar_dashboard.png", "Dashboard");
        profileButton = sidebarButton("/assets/sidebar_profile.png", "Perfil");
        departmentsButton = sidebarButton("/assets/sidebar_dependencies.png", "Dependencias");
        vacationsButton = sidebarButton("/assets/sidebar_vacations.png", "Vacaciones");
        disabilitiesButton = sidebarButton("/assets/sidebar_incapacity.png", "Incapacidades");
        permissionsButton = sidebarButton("/assets/sidebar_permissions.png", "Permisos");
        maternityButton = sidebarButton("/assets/sidebar_maternity.png", "Licencia por\nmaternidad");

        btnDashboard.setOnAction(e -> { setActive(btnDashboard); setDashboardView(); });
        profileButton.setOnAction(e -> { setActive(profileButton); setProfileView(); });
        departmentsButton.setOnAction(e -> { setActive(departmentsButton); setDepartmentsView(); });
        vacationsButton.setOnAction(e -> { setActive(vacationsButton); setRequestsView("Vacaciones", "Vacaciones"); });
        disabilitiesButton.setOnAction(e -> { setActive(disabilitiesButton); setRequestsView("Incapacidades", "Incapacidad"); });
        permissionsButton.setOnAction(e -> { setActive(permissionsButton); setRequestsView("Permisos", "Permiso"); });
        maternityButton.setOnAction(e -> { setActive(maternityButton); setRequestsView("Licencia por maternidad", "Licencia maternidad"); });

        for (Button b : List.of(btnDashboard, profileButton, departmentsButton, vacationsButton, disabilitiesButton, permissionsButton, maternityButton)) {
            VBox.setMargin(b, new Insets(0, 16, 3, 16));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        StackPane cityFrame = new StackPane();
        cityFrame.getStyleClass().add("sidebar-city-frame");
        cityFrame.setMinHeight(220);
        cityFrame.setPrefHeight(235);
        cityFrame.setMaxHeight(250);
        cityFrame.setPrefWidth(268);
        cityFrame.setMaxWidth(Double.MAX_VALUE);
        ImageView city = image("/assets/sidebar_city.png", 268, 235, false);
        city.setOpacity(0.38);
        StackPane.setAlignment(city, Pos.BOTTOM_CENTER);

        VBox cityText = new VBox(2);
        cityText.setAlignment(Pos.BOTTOM_LEFT);
        cityText.setPadding(new Insets(0, 0, 18, 14));
        Label line = new Label("Talento 360 Humano");
        line.getStyleClass().add("sidebar-city-title");
        Label sub = new Label("Gobernación de Boyacá");
        sub.getStyleClass().add("sidebar-city-subtitle");
        cityText.getChildren().addAll(line, sub);

        VBox userSummary = sidebarUserSummary();
        VBox.setMargin(userSummary, new Insets(6, 16, 0, 16));

        Region profileTopGap = new Region();
        profileTopGap.setMinHeight(6);
        profileTopGap.setPrefHeight(8);
        Region citySpacer = new Region();
        VBox.setVgrow(citySpacer, Priority.ALWAYS);
        VBox cityOverlay = new VBox(0, profileTopGap, userSummary, citySpacer, cityText);
        cityOverlay.setFillWidth(true);
        cityFrame.getChildren().addAll(city, cityOverlay);

        sidebar.getChildren().addAll(logoCard, btnDashboard, profileButton, departmentsButton, vacationsButton, disabilitiesButton, permissionsButton, maternityButton, spacer, cityFrame);
        return sidebar;
    }

    private VBox sidebarUserSummary() {
        VBox card = new VBox(7);
        card.getStyleClass().add("sidebar-user-card");
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = image("/assets/default_avatar.png", 42, 42, true);
        Circle clip = new Circle(21, 21, 21);
        avatar.setClip(clip);

        VBox texts = new VBox(2);
        Label name = new Label(authDAO.getCurrentName());
        name.getStyleClass().add("sidebar-user-name");
        name.setWrapText(true);
        Label role = new Label(authDAO.getCurrentJobTitle());
        role.getStyleClass().add("sidebar-user-role");
        role.setWrapText(true);
        Label dep = new Label(authDAO.getCurrentDepartment());
        dep.getStyleClass().add("sidebar-user-dep");
        dep.setWrapText(true);
        texts.getChildren().addAll(name, role, dep);
        row.getChildren().addAll(avatar, texts);
        card.getChildren().add(row);
        return card;
    }

    private Button sidebarButton(String assetPath, String text) {
        Button button = new Button(text);
        if (assetPath != null) {
            ImageView iv = image(assetPath, 22, 22, true);
            button.setGraphic(iv);
        }
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(13);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private Label sidebarUnicode(String emoji) {
        Label lbl = new Label(emoji);
        lbl.setStyle("-fx-font-size: 16px;");
        return lbl;
    }

    private void setActive(Button active) {
        for (Button b : List.of(btnDashboard, profileButton, departmentsButton, vacationsButton, disabilitiesButton, permissionsButton, maternityButton)) {
            if (b != null) b.getStyleClass().remove("sidebar-button-active");
        }
        if (active != null && !active.getStyleClass().contains("sidebar-button-active")) {
            active.getStyleClass().add("sidebar-button-active");
        }
    }

    private HBox topBar() {
        HBox header = new HBox();
        header.getStyleClass().add("topbar");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Talento 360 Humano");
        title.getStyleClass().add("topbar-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane avatarStack = new StackPane();
        ImageView avatar = image("/assets/default_avatar.png", 34, 34, true);
        Circle clip = new Circle(17, 17, 17);
        avatar.setClip(clip);
        avatarStack.getChildren().add(avatar);

        MenuItem logout = new MenuItem("Cerrar sesión", image("/assets/icon_logout_unique.png", 16, 16, true));
        logout.setOnAction(e -> showLogoutConfirmation());

        MenuButton userMenu = new MenuButton(authDAO.getCurrentName() + "  •  " + authDAO.getCurrentRole(), avatarStack, logout);
        userMenu.getStyleClass().add("top-user-menu");
        userMenu.setContentDisplay(ContentDisplay.LEFT);
        userMenu.setGraphicTextGap(8);

        header.getChildren().addAll(title, spacer, userMenu);
        return header;
    }

    private VBox pageShell() {
        VBox shell = new VBox(0);
        shell.getChildren().add(topBar());
        VBox.setVgrow(shell, Priority.ALWAYS);
        return shell;
    }

    private void setCenterPage(VBox shell) {
        ScrollPane scroll = new ScrollPane(shell);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("main-scroll");
        root.setCenter(scroll);
    }

    private void setDashboardView() {
        VBox shell = pageShell();
        VBox content = new VBox(18);
        content.getStyleClass().add("page-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox greetingBox = new VBox(4);
        Label greeting = new Label("¡Hola, " + authDAO.getCurrentName() + "!");
        greeting.getStyleClass().add("hero-title");
        Label description = new Label("Bienvenido(a) a Talento 360 Humano. Aquí tienes un resumen de la gestión del talento humano.");
        description.getStyleClass().add("hero-subtitle");
        greetingBox.getChildren().addAll(greeting, description);

        HBox cards = new HBox(14);
        cards.setMaxWidth(Double.MAX_VALUE);
        int employees = dashboardDAO.count("personas");
        int departments = dashboardDAO.count("dependencias");

        Node c1 = coloredStatCard("/assets/icon_people.png", "icon-bg-teal",
                "Servidores activos", format(employees), "Activos en este momento", "trend-neutral");
        Node c2 = coloredStatCard("/assets/icon_dependencias_unique.png", "icon-bg-blue",
                "Dependencias", format(departments), "Dependencias en uso", "trend-neutral");
        Node c3 = coloredStatCard("/assets/icon_cv_unique.png", "icon-bg-purple",
                "Hojas de vida", format(employees), "Hojas cargadas en la base", "trend-neutral");
        Node c4 = coloredStatCard("/assets/icon_documents_unique.png", "icon-bg-orange",
                "Documentos cargados", format(Math.max(12316, dashboardDAO.count("personas") * 4)),
                "▲ 7,1%  vs. mes anterior", "trend-label");
        for (Node c : List.of(c1, c2, c3, c4)) HBox.setHgrow(c, Priority.ALWAYS);
        cards.getChildren().addAll(c1, c2, c3, c4);

        HBox charts = dashboardCharts(employees, departments);

        HBox main = new HBox(16);
        main.setMaxWidth(Double.MAX_VALUE);

        VBox recordsCard = new VBox(12);
        recordsCard.getStyleClass().add("dashboard-table-card");
        recordsCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(recordsCard, Priority.ALWAYS);

        HBox recordsHeader = new HBox();
        recordsHeader.setAlignment(Pos.CENTER_LEFT);
        Label recordsTitle = new Label("Registros recientes");
        recordsTitle.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button export = new Button("  Exportar a Excel");
        ImageView xlsIcon = image("/assets/icon_excel.png", 15, 15, true);
        export.setGraphic(xlsIcon);
        export.getStyleClass().add("green-small-button");
        recordsHeader.getChildren().addAll(recordsTitle, spacer, export);

        final int pageSize = 20;
        List<Map<String, String>> dashboardRows = new ArrayList<>(dashboardDAO.records(100));
        final int maxDashboardPages = Math.min(5, Math.max(1, (int) Math.ceil(dashboardRows.size() / (double) pageSize)));
        final int[] currentPage = {1};
        TableView<Map<String, String>> table = createDashboardTable(pageSlice(dashboardRows, currentPage[0], pageSize));
        table.getStyleClass().add("dashboard-table");
        table.setPrefHeight(650);
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);
        export.setOnAction(e -> exportMapTable(table));

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(4, 0, 0, 0));
        Button all = new Button("Ver todos los registros  ›");
        all.getStyleClass().add("link-button");
        boolean[] showingAll = {false};
        Region paginationSpacer = new Region();
        HBox.setHgrow(paginationSpacer, Priority.ALWAYS);
        HBox pageButtons = new HBox(8);
        pageButtons.setAlignment(Pos.CENTER);
        Label chevLeft = new Label("‹");
        chevLeft.getStyleClass().add("pagination-arrow");
        Label chevRight = new Label("›");
        chevRight.getStyleClass().add("pagination-arrow");
        List<Button> dashboardPageButtons = new ArrayList<>();
        pageButtons.getChildren().add(chevLeft);
        for (int i = 1; i <= maxDashboardPages; i++) {
            final int pageNumber = i;
            Button pageButton = pageBtn(String.valueOf(i), i == 1);
            pageButton.setTooltip(new Tooltip("Página " + i + " de registros recientes. Entre mayor sea el número, más antiguos son los registros."));
            pageButton.setOnAction(e -> {
                if (showingAll[0]) return;
                currentPage[0] = pageNumber;
                table.getItems().setAll(pageSlice(dashboardRows, currentPage[0], pageSize));
                updatePageButtons(dashboardPageButtons, currentPage[0]);
            });
            dashboardPageButtons.add(pageButton);
            pageButtons.getChildren().add(pageButton);
        }
        pageButtons.getChildren().add(chevRight);

        Runnable refreshDashboardPage = () -> {
            table.getItems().setAll(pageSlice(dashboardRows, currentPage[0], pageSize));
            updatePageButtons(dashboardPageButtons, currentPage[0]);
        };
        chevLeft.setOnMouseClicked(e -> {
            if (showingAll[0]) return;
            if (currentPage[0] > 1) {
                currentPage[0]--;
                refreshDashboardPage.run();
            }
        });
        chevRight.setOnMouseClicked(e -> {
            if (showingAll[0]) return;
            if (currentPage[0] < maxDashboardPages) {
                currentPage[0]++;
                refreshDashboardPage.run();
            }
        });
        all.setOnAction(e -> {
            showingAll[0] = !showingAll[0];
            if (showingAll[0]) {
                table.getItems().setAll(dashboardRows);
                all.setText("Ver menos registros  ›");
                dashboardPageButtons.forEach(b -> b.setDisable(true));
            } else {
                currentPage[0] = 1;
                refreshDashboardPage.run();
                all.setText("Ver todos los registros  ›");
                dashboardPageButtons.forEach(b -> b.setDisable(false));
            }
        });
        footer.getChildren().addAll(all, paginationSpacer, pageButtons);
        recordsCard.getChildren().addAll(recordsHeader, table, footer);

        VBox employee = employeeCard();
        main.getChildren().addAll(recordsCard, employee);

        content.getChildren().addAll(greetingBox, cards, charts, main, footerBar());
        shell.getChildren().add(content);
        setCenterPage(shell);
    }

    private HBox dashboardCharts(int employees, int departments) {
        List<Map<String, String>> rows = dashboardDAO.records(260);
        long vacationsCount = rows.stream().filter(r -> "Vacaciones".equals(r.get("Proceso"))).count();
        long incapacidades = rows.stream().filter(r -> "Incapacidad".equals(r.get("Proceso"))).count();
        long permisos = rows.stream().filter(r -> "Permiso".equals(r.get("Proceso"))).count();
        long hojasVida = rows.stream().filter(r -> "Hoja de vida".equals(r.get("Proceso"))).count();
        long maternidad = rows.stream().filter(r -> "Licencia maternidad".equals(r.get("Proceso"))).count();

        VBox barCard = new VBox(10);
        barCard.getStyleClass().add("chart-card");
        Label barTitle = new Label("Registros por proceso");
        barTitle.getStyleClass().add("section-title");
        Label barHelp = new Label("La gráfica de barras compara la cantidad de registros por cada proceso del sistema.");
        barHelp.getStyleClass().add("chart-helper");
        Label barSummary = new Label("Total: " + rows.size() + " registros");
        barSummary.getStyleClass().add("chart-summary");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Tipo de solicitud");
        yAxis.setLabel("Cantidad");
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setLegendVisible(false);
        bar.setAnimated(true);
        bar.setPrefHeight(315);
        bar.setCategoryGap(22);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Vacaciones", vacationsCount));
        series.getData().add(new XYChart.Data<>("Incapacidades", incapacidades));
        series.getData().add(new XYChart.Data<>("Permisos", permisos));
        series.getData().add(new XYChart.Data<>("Maternidad", maternidad));
        series.getData().add(new XYChart.Data<>("Hojas de vida", hojasVida));
        bar.getData().add(series);
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((noteText, oldNode, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(data.getXValue() + ": " + data.getYValue() + " registros"));
                    node.setOnMouseClicked(e -> barSummary.setText(data.getXValue() + ": " + data.getYValue() + " registros"));
                }
            });
        }
        barCard.getChildren().addAll(barTitle, barHelp, bar, barSummary);

        VBox pieCard = new VBox(10);
        pieCard.getStyleClass().add("chart-card");
        Label pieTitle = new Label("Distribución general");
        pieTitle.getStyleClass().add("section-title");
        Label pieHelp = new Label("La gráfica de torta muestra la distribución general entre servidores, dependencias, solicitudes y documentos.");
        pieHelp.getStyleClass().add("chart-helper");
        Label pieSummary = new Label("Distribución general del sistema por categoría.");
        pieSummary.getStyleClass().add("chart-summary");
        PieChart pie = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Servidores", Math.max(employees, 1)),
                new PieChart.Data("Dependencias", Math.max(departments, 1)),
                new PieChart.Data("Solicitudes", Math.max(rows.size(), 1)),
                new PieChart.Data("Documentos", Math.max(rows.size() * 3, 1))
        ));
        pie.setLegendVisible(true);
        pie.setLabelsVisible(true);
        pie.setAnimated(true);
        pie.setPrefHeight(315);
        pie.getData().forEach(data -> data.nodeProperty().addListener((noteText, oldNode, node) -> {
            if (node != null) {
                Tooltip.install(node, new Tooltip(data.getName() + ": " + (int) data.getPieValue()));
                node.setOnMouseClicked(e -> pieSummary.setText(data.getName() + ": " + (int) data.getPieValue() + " registros"));
            }
        }));
        pieCard.getChildren().addAll(pieTitle, pieHelp, pie, pieSummary);

        HBox charts = new HBox(16, barCard, pieCard);
        charts.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(barCard, Priority.ALWAYS);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        return charts;
    }

    private Button pageBtn(String text, boolean active) {
        Button btn = new Button(text);
        btn.getStyleClass().add(active ? "page-btn" : "page-btn-inactive");
        return btn;
    }

    private void updatePageButtons(List<Button> buttons, int activePage) {
        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            btn.getStyleClass().removeAll("page-btn", "page-btn-inactive");
            btn.getStyleClass().add(i + 1 == activePage ? "page-btn" : "page-btn-inactive");
        }
    }

    private VBox coloredStatCard(String iconPath, String bgClass, String title, String value, String trend, String trendClass) {
        HBox card = new HBox(14);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        StackPane iconBg = new StackPane();
        iconBg.getStyleClass().add(bgClass);
        ImageView iv = image(iconPath, 30, 30, true);
        iconBg.getChildren().add(iv);

        VBox text = new VBox(4);
        Label t = new Label(title);
        t.getStyleClass().add("stat-title");
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        Label tr = new Label(trend);
        tr.getStyleClass().add(trendClass);
        text.getChildren().addAll(t, v, tr);
        card.getChildren().addAll(iconBg, text);
        VBox wrap = new VBox(card);
        HBox.setHgrow(wrap, Priority.ALWAYS);
        return wrap;
    }

    private VBox employeeCard() {
        VBox box = new VBox(12);
        box.getStyleClass().add("employee-card");
        box.setPrefWidth(305);
        box.setMinWidth(285);
        box.setAlignment(Pos.TOP_CENTER);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label empTitle = new Label("Empleado del mes");
        empTitle.getStyleClass().add("section-title");
        Region trSpacer = new Region();
        HBox.setHgrow(trSpacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(empTitle, trSpacer);

        Region shortGold = new Region();
        shortGold.getStyleClass().add("employee-gold-line");

        StackPane medalStack = new StackPane();
        medalStack.getStyleClass().add("employee-medal-box");
        ImageView medal = image("/assets/employee_medal.png", 210, 175, true);
        medal.setSmooth(true);
        medalStack.getChildren().add(medal);
        medalStack.setPrefHeight(175);

        Label name = new Label("María González");
        name.getStyleClass().add("employee-name");
        Label role = new Label("Profesional Universitario");
        role.getStyleClass().add("employee-role");

        Separator sep = new Separator();
        sep.getStyleClass().add("gold-separator");

        HBox trophyRow = new HBox(12);
        trophyRow.getStyleClass().add("recognition-box");
        trophyRow.setAlignment(Pos.CENTER_LEFT);
        ImageView trophyIcon = image("/assets/icon_trophy_unique.png", 34, 34, true);
        Label text = new Label("Reconocimiento por su\ncompromiso, dedicación\ny aporte al desarrollo\ninstitucional.");
        text.getStyleClass().add("employee-text");
        text.setWrapText(true);
        trophyRow.getChildren().addAll(trophyIcon, text);

        Button btn = new Button("Ver reconocimiento");
        btn.setGraphic(image("/assets/icon_trophy_unique.png", 16, 16, true));
        btn.getStyleClass().add("green-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showAlert("Empleado del mes", "María González · Profesional Universitario\n\nReconocimiento por su compromiso, dedicación y aporte al desarrollo institucional."));

        box.getChildren().addAll(titleRow, shortGold, medalStack, name, role, sep, trophyRow, btn);
        return box;
    }

    private TableView<Map<String, String>> createDashboardTable(List<Map<String, String>> rows) {
        TableView<Map<String, String>> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(rows));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        if (rows.isEmpty()) return table;

        for (String key : rows.get(0).keySet()) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(key);
            col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrDefault(key, "")));

            if (key.equals("Proceso")) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label tag = new Label(procesoIcon(item) + " " + item);
                        tag.getStyleClass().add(procesoTagClass(item));
                        setGraphic(tag);
                        setText(null);
                    }
                });
            } else if (key.equals("Estado")) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label("● " + item);
                        badge.getStyleClass().add(dashboardBadgeClass(item));
                        setGraphic(badge);
                        setText(null);
                    }
                });
            }

            col.setPrefWidth(switch (key) {
                case "Descripción", "Dependencia", "Servidor" -> 190;
                case "Radicado" -> 150;
                case "Estado" -> 125;
                case "Proceso" -> 150;
                case "Fecha" -> 135;
                default -> 95;
            });
            table.getColumns().add(col);
        }
        return table;
    }

    private String procesoIcon(String proceso) {
        return switch (proceso) {
            case "Hoja de vida" -> "📄";
            case "Permiso" -> "📋";
            case "Vacaciones" -> "🏖";
            case "Incapacidad" -> "🏥";
            case "Licencia maternidad" -> "👩";
            default -> "📁";
        };
    }

    private String procesoTagClass(String proceso) {
        return switch (proceso) {
            case "Hoja de vida" -> "tag-hoja";
            case "Permiso" -> "tag-permiso";
            case "Vacaciones" -> "tag-vacaciones";
            case "Licencia maternidad" -> "tag-maternidad";
            default -> "tag-incapacidad";
        };
    }

    private String dashboardBadgeClass(String status) {
        if (status == null) return "badge-pendiente";
        String e = status.trim().toLowerCase();
        if (e.contains("aprob") || e.contains("complet")) return "badge-completado";
        if (e.contains("final")) return "badge-finalizada";
        if (e.contains("rechaz")) return "badge-rechazada";
        if (e.contains("revisión") || e.contains("revision")) return "badge-revision";
        return "badge-pendiente";
    }

    private void setProfileView() {
        VBox shell = pageShell();
        VBox content = new VBox(16);
        content.getStyleClass().add("page-content");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label title = new Label("Resumen de perfil");
        title.getStyleClass().add("hero-title");
        titleBox.getChildren().add(title);
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button newUserButton = new Button("  Nuevo usuario");
        newUserButton.setGraphic(image("/assets/icon_plus_unique.png", 17, 17, true));
        newUserButton.getStyleClass().add("green-button");
        titleRow.getChildren().addAll(titleBox, titleSpacer, newUserButton);

        VBox searchCard = new VBox(12);
        searchCard.getStyleClass().add("search-card");
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #4a5a75;");
        TextField search = new TextField();
        search.setPromptText("Buscar por nombre, cédula, dependencia o cargo...");
        search.getStyleClass().add("input");
        search.setPrefWidth(500);
        HBox.setHgrow(search, Priority.ALWAYS);
        Button searchButton = new Button("Buscar");
        searchButton.getStyleClass().add("primary-button");
        searchRow.getChildren().addAll(searchIcon, search, searchButton);

        TableView<Employee> table = createEmployeeTable(employeeDAO.list(""));
        table.setMinHeight(210);
        table.setPrefHeight(235);
        table.setMaxHeight(260);
        VBox.setVgrow(table, Priority.NEVER);
        searchCard.setMinHeight(0);
        searchCard.getChildren().addAll(searchRow, table);

        VBox details = new VBox(16);
        details.setPadding(new Insets(8, 0, 0, 0));

        Runnable selectFirst = () -> {
            if (!table.getItems().isEmpty()) table.getSelectionModel().select(0);
            else details.getChildren().setAll(emptyProfileCard());
        };

        table.getSelectionModel().selectedItemProperty().addListener((noteText, old, selected) -> {
            if (selected != null) details.getChildren().setAll(buildProfileContent(selected));
        });

        searchButton.setOnAction(e -> {
            List<Employee> result = employeeDAO.list(search.getText());
            table.setItems(FXCollections.observableArrayList(result));
            selectFirst.run();
        });
        newUserButton.setOnAction(e -> showNewUserDialog(table, details, search));
        search.setOnAction(e -> searchButton.fire());
        selectFirst.run();

        content.getChildren().addAll(titleRow, searchCard, details);
        shell.getChildren().add(content);
        setCenterPage(shell);
    }

    private void setDepartmentsView() {
        VBox shell = pageShell();
        VBox content = new VBox(16);
        content.getStyleClass().add("page-content");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Dependencias");
        title.getStyleClass().add("hero-title");
        Label subtitle = new Label("Consulta rápida de perfiles directivos demo según la estructura general de secretarías y áreas de la Gobernación de Boyacá.");
        subtitle.getStyleClass().add("hero-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button addDepartmentButton = new Button("  Agregar dependencia");
        addDepartmentButton.setGraphic(image("/assets/icon_plus_unique.png", 17, 17, true));
        addDepartmentButton.getStyleClass().add("green-button");
        titleRow.getChildren().addAll(titleBox, titleSpacer, addDepartmentButton);

        VBox searchCard = new VBox(12);
        searchCard.getStyleClass().add("search-card");
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: #23425f;");
        TextField search = new TextField();
        search.setPromptText("Buscar por dependencia, cargo, nombre o correo...");
        search.getStyleClass().add("input");
        HBox.setHgrow(search, Priority.ALWAYS);
        Button searchButton = new Button("Buscar");
        searchButton.getStyleClass().add("primary-button");
        searchRow.getChildren().addAll(searchIcon, search, searchButton);

        VBox details = new VBox(16);
        TableView<Map<String, String>> table = createManagersTable(departmentsData(""));
        table.setPrefHeight(500);
        searchCard.getChildren().addAll(searchRow, table);

        Runnable selectFirst = () -> {
            if (!table.getItems().isEmpty()) table.getSelectionModel().select(0);
            else details.getChildren().setAll(emptyManagerCard());
        };

        table.getSelectionModel().selectedItemProperty().addListener((noteText, old, selected) -> {
            if (selected != null) details.getChildren().setAll(buildManagerProfile(selected));
        });

        searchButton.setOnAction(e -> {
            table.setItems(FXCollections.observableArrayList(departmentsData(search.getText())));
            selectFirst.run();
        });
        addDepartmentButton.setOnAction(e -> showAddDepartmentDialog(table, details, search));
        search.setOnAction(e -> searchButton.fire());
        selectFirst.run();

        content.getChildren().addAll(titleRow, searchCard, details, footerBar());
        shell.getChildren().add(content);
        setCenterPage(shell);
    }

    private void showNewUserDialog(TableView<Employee> table, VBox details, TextField search) {
        if (appFrame == null) return;

        VBox card = new VBox(14);
        card.getStyleClass().add("modal-card-large");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_profile_unique.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box");
        VBox headerTexts = new VBox(3);
        Label title = new Label("Nuevo usuario");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Registra un servidor en perfiles y guarda la relacion principal en la base de datos.");
        subtitle.getStyleClass().add("modal-subtitle");
        headerTexts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, headerTexts);

        GridPane form = new GridPane();
        form.getStyleClass().add("decorated-form");
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        TextField fullName = decoratedTextField("Nombre completo");
        TextField document = decoratedTextField("Cedula");
        ComboBox<String> department = editableCombo(departmentDAO.listDepartmentNames(), "Dependencia");
        ComboBox<String> jobTitle = editableCombo(departmentDAO.listJobTitleNames(), "Cargo");
        ComboBox<String> gender = new ComboBox<>(FXCollections.observableArrayList("F", "M", "NO REGISTRADO"));
        gender.setValue("NO REGISTRADO");
        gender.getStyleClass().add("combo");
        gender.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("ACTIVO", "ENCARGO", "PROVISIONAL", "RETIRADO"));
        status.setValue("ACTIVO");
        status.getStyleClass().add("combo");
        status.setMaxWidth(Double.MAX_VALUE);
        DatePicker startDate = datePicker("Fecha de ingreso");
        startDate.setPrefWidth(260);
        TextField email = decoratedTextField("Correo institucional");
        TextField phone = decoratedTextField("Celular");

        form.add(formField("Nombre completo", fullName), 0, 0);
        form.add(formField("Cedula", document), 1, 0);
        form.add(formField("Dependencia", department), 0, 1);
        form.add(formField("Cargo", jobTitle), 1, 1);
        form.add(formField("Genero", gender), 0, 2);
        form.add(formField("Situacion", status), 1, 2);
        form.add(formField("Fecha de ingreso", startDate), 0, 3);
        form.add(formField("Correo institucional", email), 1, 3);
        form.add(formField("Celular", phone), 0, 4);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button save = new Button("Guardar usuario");
        save.getStyleClass().add("green-button");
        actions.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, form, actions);
        StackPane overlay = modalOverlay(card);

        cancel.setOnAction(e -> closeOverlay(overlay));
        save.setOnAction(e -> {
            String fullNameText = fullName.getText() == null ? "" : fullName.getText().trim();
            String documentText = document.getText() == null ? "" : document.getText().trim();
            String departmentText = comboText(department);
            String jobTitleText = comboText(jobTitle);
            if (fullNameText.isBlank() || documentText.isBlank() || departmentText.isBlank() || jobTitleText.isBlank()) {
                showAlert("Formulario incompleto", "Completa nombre, cedula, dependencia y cargo antes de guardar.");
                return;
            }
            if (employeeDAO.documentExists(documentText)) {
                showAlert("Usuario existente", "Ya existe un perfil con la cedula " + documentText + ".");
                return;
            }
            Employee employee = new Employee(
                    "",
                    documentText,
                    upperUi(fullNameText),
                    upperUi(departmentText),
                    upperUi(jobTitleText),
                    upperUi(jobTitleText),
                    email.getText() == null ? "" : email.getText().trim(),
                    phone.getText() == null ? "" : phone.getText().trim(),
                    comboText(status),
                    formatUiDate(startDate.getValue()),
                    comboText(gender)
            );
            if (employeeDAO.create(employee)) {
                closeOverlay(overlay);
                search.setText("");
                List<Employee> result = employeeDAO.list("");
                table.setItems(FXCollections.observableArrayList(result));
                selectEmployeeByDocument(table, details, documentText);
                showAlert("Usuario registrado", "El usuario fue guardado correctamente en PostgreSQL.\n\nNombre: " + upperUi(fullNameText) + "\nCedula: " + documentText);
            } else {
                showAlert("No se pudo guardar", "No se pudo guardar el usuario en la base de datos. Revisa la conexion, el script final y que la cedula no exista.");
            }
        });
    }

    private void selectEmployeeByDocument(TableView<Employee> table, VBox details, String document) {
        if (table == null || table.getItems().isEmpty()) {
            if (details != null) details.getChildren().setAll(emptyProfileCard());
            return;
        }
        for (Employee item : table.getItems()) {
            if (item.getDocumentId() != null && item.getDocumentId().equals(document)) {
                table.getSelectionModel().select(item);
                if (details != null) details.getChildren().setAll(buildProfileContent(item));
                return;
            }
        }
        table.getSelectionModel().select(0);
        if (details != null) details.getChildren().setAll(buildProfileContent(table.getItems().get(0)));
    }

    private void showAddDepartmentDialog(TableView<Map<String, String>> table, VBox details, TextField search) {
        if (appFrame == null) return;

        VBox card = new VBox(14);
        card.getStyleClass().add("modal-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_dependencias_unique.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box");
        VBox headerTexts = new VBox(3);
        Label title = new Label("Agregar dependencia");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Crea una nueva dependencia en el catalogo institucional.");
        subtitle.getStyleClass().add("modal-subtitle");
        headerTexts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, headerTexts);

        TextField dependencyName = decoratedTextField("Nombre de la dependencia");
        VBox form = new VBox(10, formField("Dependencia", dependencyName));
        form.getStyleClass().add("decorated-form");
        form.setPadding(new Insets(16));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button save = new Button("Guardar dependencia");
        save.getStyleClass().add("green-button");
        actions.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, form, actions);
        StackPane overlay = modalOverlay(card);

        cancel.setOnAction(e -> closeOverlay(overlay));
        save.setOnAction(e -> {
            String value = dependencyName.getText() == null ? "" : dependencyName.getText().trim();
            if (value.isBlank()) {
                showAlert("Formulario incompleto", "Ingresa el nombre de la dependencia.");
                return;
            }
            if (departmentDAO.create(value)) {
                closeOverlay(overlay);
                search.setText("");
                table.setItems(FXCollections.observableArrayList(departmentsData("")));
                selectDepartmentByName(table, details, value);
                showAlert("Dependencia registrada", "La dependencia fue guardada correctamente en PostgreSQL.\n\nDependencia: " + upperUi(value));
            } else {
                showAlert("No se pudo guardar", "No se pudo guardar la dependencia. Revisa la conexion o si ya existe en la base de datos.");
            }
        });
    }

    private void selectDepartmentByName(TableView<Map<String, String>> table, VBox details, String name) {
        if (table == null || table.getItems().isEmpty()) {
            if (details != null) details.getChildren().setAll(emptyManagerCard());
            return;
        }
        String key = normalizeLookup(name);
        for (Map<String, String> row : table.getItems()) {
            if (normalizeLookup(row.getOrDefault("Dependencia", "")).equals(key)) {
                table.getSelectionModel().select(row);
                if (details != null) details.getChildren().setAll(buildManagerProfile(row));
                return;
            }
        }
        table.getSelectionModel().select(0);
        if (details != null) details.getChildren().setAll(buildManagerProfile(table.getItems().get(0)));
    }


    private List<Map<String, String>> departmentsData(String filter) {
        List<Map<String, String>> rows = departmentDAO.list(filter);
        if (rows == null || rows.isEmpty()) {
            return filterManagers(filter);
        }
        return rows;
    }

    private TableView<Map<String, String>> createManagersTable(List<Map<String, String>> rows) {
        TableView<Map<String, String>> table = new TableView<>(FXCollections.observableArrayList(rows));
        table.getStyleClass().add("profile-search-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addMapColumn(table, "Dependencia", "Dependencia", 310);
        addMapColumn(table, "Cargo directivo", "Cargo", 260);
        addMapColumn(table, "Nombre", "Nombre", 240);
        addMapColumn(table, "Correo", "Correo", 240);
        addMapColumn(table, "Ext.", "Extension", 85);
        return table;
    }

    private void addMapColumn(TableView<Map<String, String>> table, String title, String key, int width) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrDefault(key, "")));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }

    private List<Map<String, String>> filterManagers(String filter) {
        List<Map<String, String>> baseRows = demoManagers();
        if (filter == null || filter.isBlank()) return baseRows;
        String f = filter.toLowerCase();
        return baseRows.stream()
                .filter(r -> r.values().stream().anyMatch(v -> v != null && v.toLowerCase().contains(f)))
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> demoManagers() {
        List<Map<String, String>> rows = new ArrayList<>();
        String[][] data = {
                {"DESPACHO DEL GOBERNADOR", "GOBERNADOR / JEFE DE DESPACHO", "ROJAS HERRERA ANDRÉS FELIPE", "despacho.gobernador@boyaca.gov.co", "2214", "DIRECCIÓN ESTRATÉGICA INSTITUCIONAL Y COORDINACIÓN DEL GABINETE DEPARTAMENTAL."},
                {"SECRETARÍA DE PLANEACIÓN", "SECRETARIA DE PLANEACIÓN", "MÉNDEZ ROJAS LAURA CAMILA", "planeacion@boyaca.gov.co", "2213", "PLANEACIÓN TERRITORIAL, SEGUIMIENTO A PROYECTOS Y APOYO A LA TOMA DE DECISIONES."},
                {"SECRETARÍA DE HACIENDA", "SECRETARIO DE HACIENDA", "AGUILAR MORENO CARLOS EDUARDO", "hacienda@boyaca.gov.co", "2125", "GESTIÓN PRESUPUESTAL, FINANCIERA Y TRIBUTARIA DEL DEPARTAMENTO."},
                {"SECRETARÍA DE CONTRATACIÓN", "SECRETARIA DE CONTRATACIÓN", "PRIETO GARCÍA MÓNICA ALEJANDRA", "contratacion@boyaca.gov.co", "2112", "PROCESOS CONTRACTUALES, SECOP Y ACOMPAÑAMIENTO JURÍDICO ADMINISTRATIVO."},
                {"SECRETARÍA GENERAL", "SECRETARIO GENERAL", "MORALES NIÑO JUAN PABLO", "secretaria.general@boyaca.gov.co", "2119", "SOPORTE ADMINISTRATIVO TRANSVERSAL, GESTIÓN DOCUMENTAL Y SERVICIOS INTERNOS."},
                {"SECRETARÍA DE GOBIERNO Y ACCIÓN COMUNAL", "SECRETARIA DE GOBIERNO", "TORRES RIVERA DIANA MARCELA", "gobierno@boyaca.gov.co", "2155", "GOBERNABILIDAD, ACCIÓN COMUNAL Y COORDINACIÓN CON MUNICIPIOS."},
                {"SECRETARÍA DE INTEGRACIÓN SOCIAL", "SECRETARIA DE INTEGRACIÓN SOCIAL", "SILVA PARRA NATALIA FERNANDA", "integracionsocial@boyaca.gov.co", "2220", "PROGRAMAS SOCIALES, ATENCIÓN POBLACIONAL Y ARTICULACIÓN COMUNITARIA."},
                {"SECRETARÍA DE EDUCACIÓN", "SECRETARIO DE EDUCACIÓN", "CASTRO DÍAZ MIGUEL ÁNGEL", "educacion@boyaca.gov.co", "3101", "GESTIÓN DEL SISTEMA EDUCATIVO DEPARTAMENTAL Y TALENTO HUMANO DOCENTE."},
                {"SECRETARÍA DE CULTURA Y PATRIMONIO", "SECRETARIA DE CULTURA", "PARDO RUIZ VALENTINA", "cultura@boyaca.gov.co", "2212", "PROMOCIÓN CULTURAL, PATRIMONIO Y PROCESOS ARTÍSTICOS REGIONALES."},
                {"SECRETARÍA DE SALUD", "SECRETARIA DE SALUD", "GÓMEZ RUIZ PAULA ANDREA", "salud@boyaca.gov.co", "4115", "SALUD PÚBLICA, RED DE SERVICIOS Y PROGRAMAS DE PREVENCIÓN."},
                {"SECRETARÍA DE INFRAESTRUCTURA PÚBLICA", "SECRETARIO DE INFRAESTRUCTURA", "CÁRDENAS LÓPEZ JORGE IVÁN", "infraestructura@boyaca.gov.co", "2254", "INFRAESTRUCTURA VIAL, OBRAS PÚBLICAS Y SEGUIMIENTO TÉCNICO."},
                {"SECRETARÍA DE MINAS Y ENERGÍA", "SECRETARIA DE MINAS Y ENERGÍA", "NIÑO CORTÉS CLAUDIA PATRICIA", "minasenergia@boyaca.gov.co", "7402594", "GESTIÓN MINERA, ENERGÉTICA Y COORDINACIÓN SECTORIAL."},
                {"SECRETARÍA DE AMBIENTE Y DESARROLLO SOSTENIBLE", "SECRETARIO DE AMBIENTE", "HERRERA LÓPEZ SANTIAGO", "ambiente@boyaca.gov.co", "2353", "GESTIÓN AMBIENTAL, SOSTENIBILIDAD Y COORDINACIÓN CLIMÁTICA."},
                {"SECRETARÍA DE AGRICULTURA", "SECRETARIA DE AGRICULTURA", "RODRÍGUEZ NIÑO MARÍA CAMILA", "agricultura@boyaca.gov.co", "2367", "DESARROLLO AGROPECUARIO, ASISTENCIA TÉCNICA Y ARTICULACIÓN PRODUCTIVA."},
                {"SECRETARÍA DE DESARROLLO EMPRESARIAL", "SECRETARIO DE DESARROLLO EMPRESARIAL", "SUÁREZ MEJÍA DANIEL ESTEBAN", "desarrolloempresarial@boyaca.gov.co", "2132", "EMPRENDIMIENTO, COMPETITIVIDAD, PRODUCTIVIDAD E INNOVACIÓN EMPRESARIAL."},
                {"SECRETARÍA DE TURISMO", "SECRETARIA DE TURISMO", "RINCÓN DUARTE CAROLINA", "turismo@boyaca.gov.co", "2203", "PROMOCIÓN TURÍSTICA, RUTAS Y FORTALECIMIENTO DE DESTINOS."},
                {"SECRETARÍA DE TIC Y GOBIERNO ABIERTO", "SECRETARIA TIC", "ROBAYO SÁNCHEZ SANDRA MILENA", "tic@boyaca.gov.co", "2275", "GOBIERNO DIGITAL, DATOS, TECNOLOGÍA Y TRANSPARENCIA INSTITUCIONAL."}
        };
        for (String[] d : data) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("Dependencia", d[0]);
            row.put("Cargo", d[1]);
            row.put("Nombre", d[2]);
            row.put("Correo", d[3]);
            row.put("Extension", d[4]);
            row.put("Funciones", d[5]);
            row.put("Ciudad", "Tunja, Boyacá");
            row.put("Estado", "Activo");
            rows.add(row);
        }
        return rows;
    }

    private Node buildManagerProfile(Map<String, String> selected) {
        VBox box = new VBox(16);
        HBox header = new HBox(22);
        header.getStyleClass().add("profile-hero");
        header.setAlignment(Pos.CENTER_LEFT);
        ImageView avatar = image("/assets/default_avatar.png", 96, 96, true);
        VBox person = new VBox(7);
        Label name = new Label(selected.getOrDefault("Nombre", "No registrado"));
        name.getStyleClass().add("profile-name");
        Label jobTitle = new Label(selected.getOrDefault("Cargo", "No registrado"));
        jobTitle.getStyleClass().add("profile-sub");
        Label dep = new Label(selected.getOrDefault("Dependencia", "No registrada"));
        dep.getStyleClass().add("profile-sub");
        Label badge = new Label("● Perfil directivo demo");
        badge.getStyleClass().add("status-approved");
        person.getChildren().addAll(name, jobTitle, dep, badge);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox meta = new VBox(12,
                metaRow("/assets/icon_contact_unique.png", "Correo", selected.getOrDefault("Correo", "No registrado")),
                metaRow("/assets/icon_calendar_unique.png", "Extensión", selected.getOrDefault("Extension", "No registrada"))
        );
        meta.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().addAll(avatar, person, spacer, meta);

        HBox cards = new HBox(16);
        VBox institutionalCard = infoCard("/assets/icon_dependencias_unique.png", "Datos de la dependencia",
                row("Dependencia", selected.getOrDefault("Dependencia", "")),
                row("Ciudad", selected.getOrDefault("Ciudad", "")),
                row("Estado", selected.getOrDefault("Estado", ""))
        );
        VBox managerCard = infoCard("/assets/icon_profile_unique.png", "Perfil del cargo",
                row("Cargo", selected.getOrDefault("Cargo", "")),
                row("Nombre", selected.getOrDefault("Nombre", "")),
                row("Correo", selected.getOrDefault("Correo", ""))
        );
        VBox functionsCard = infoCard("/assets/icon_history_unique.png", "Funciones resumidas",
                row("Rol principal", selected.getOrDefault("Funciones", "")),
                row("Permisos", authDAO.canEditRequests() ? "Edición habilitada según rol" : "Consulta y descarga"),
                row("Fuente", "Estructura institucional + datos demo")
        );
        for (VBox c : List.of(institutionalCard, managerCard, functionsCard)) HBox.setHgrow(c, Priority.ALWAYS);
        cards.getChildren().addAll(institutionalCard, managerCard, functionsCard);
        box.getChildren().addAll(header, cards);
        return box;
    }

    private Node emptyManagerCard() {
        VBox box = new VBox(10);
        box.getStyleClass().add("info-card-wide");
        box.setAlignment(Pos.CENTER);
        Label icon = new Label("🏛");
        icon.setStyle("-fx-font-size: 36px;");
        Label msg = new Label("No se encontraron dependencias con ese criterio.");
        msg.getStyleClass().add("section-title");
        box.getChildren().addAll(icon, msg);
        return box;
    }

    private TableView<Employee> createEmployeeTable(List<Employee> employees) {
        TableView<Employee> table = new TableView<>(FXCollections.observableArrayList(employees));
        table.getStyleClass().add("profile-search-table");
        table.getStyleClass().add("perfil-summary-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addColumn(table, "Cédula", Employee::getDocumentId, 125);
        addColumn(table, "Nombre", Employee::getFullName, 240);
        addColumn(table, "Dependencia", Employee::getDepartment, 260);
        addColumn(table, "Cargo actual", Employee::getCurrentJobTitle, 220);

        TableColumn<Employee, String> statusCol = new TableColumn<>("Situación");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmploymentStatus()));
        statusCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(statusBadge(item.toUpperCase(), employeeStatusBadgeClass(item)));
                setText(null);
            }
        });
        statusCol.setPrefWidth(130);
        table.getColumns().add(statusCol);
        return table;
    }

    private String employeeStatusBadgeClass(String status) {
        if (status == null) return "badge-pendiente";
        String e = status.toLowerCase();
        if (e.contains("retir") || e.contains("inactivo")) return "badge-rechazada";
        if (e.contains("encargo") || e.contains("provisional")) return "badge-revision";
        return "badge-aprobada";
    }

    private VBox buildProfileContent(Employee selected) {
        VBox box = new VBox(16);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox cards = new HBox(16);
        cards.setMaxWidth(Double.MAX_VALUE);
        VBox personalInfo = infoCard("/assets/icon_profile_unique.png", "Información personal",
                row("Documento de identidad", value(selected, Employee::getDocumentId)),
                row("Fecha de nacimiento", "No registrada"),
                row("Lugar de nacimiento", "Tunja, Boyacá"),
                row("Género", value(selected, Employee::getGender)),
                row("Estado civil", "No registrado")
        );
        VBox workInfo = infoCard("/assets/icon_labor_unique.png", "Información laboral",
                row("Dependencia", value(selected, Employee::getDepartment)),
                row("Cargo", value(selected, Employee::getCurrentJobTitle)),
                row("Cargo base", value(selected, Employee::getBaseJobTitle)),
                row("Fecha de ingreso", value(selected, Employee::getStartDate)),
                row("Tipo de vinculación", value(selected, Employee::getEmploymentStatus))
        );
        VBox contactInfo = infoCard("/assets/icon_contact_unique.png", "Información de contacto",
                row("Correo institucional", value(selected, Employee::getEmail)),
                row("Correo personal", "No registrado"),
                row("Teléfono celular", value(selected, Employee::getPhone)),
                row("Teléfono fijo", "No registrado"),
                row("Ciudad", "Tunja, Boyacá")
        );
        for (VBox c : List.of(personalInfo, workInfo, contactInfo)) HBox.setHgrow(c, Priority.ALWAYS);
        cards.getChildren().addAll(personalInfo, workInfo, contactInfo);
        box.getChildren().addAll(profileHeader(selected), cards);
        return box;
    }

    private Node emptyProfileCard() {
        VBox box = new VBox(10);
        box.getStyleClass().add("info-card-wide");
        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 36px;");
        Label t = new Label("Selecciona un servidor para ver el resumen");
        t.getStyleClass().add("section-title");
        Label msg = new Label("Busca por nombre, cédula, dependencia o cargo para actualizar los paneles del perfil.");
        msg.getStyleClass().add("muted");
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(icon, t, msg);
        return box;
    }

    private VBox profileHeader(Employee s) {
        HBox header = new HBox(22);
        header.getStyleClass().add("profile-hero");
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarStack = new StackPane();
        ImageView avatar = image("/assets/default_avatar.png", 100, 100, true);
        avatarStack.getChildren().add(avatar);

        VBox person = new VBox(7);
        Label name = new Label(value(s, Employee::getFullName));
        name.getStyleClass().add("profile-name");
        Label jobTitle = new Label(value(s, Employee::getCurrentJobTitle));
        jobTitle.getStyleClass().add("profile-sub");

        HBox depRow = new HBox(6);
        depRow.setAlignment(Pos.CENTER_LEFT);
        ImageView buildingIcon = image("/assets/icon_dependencias_unique.png", 15, 15, true);
        Label dep = new Label(value(s, Employee::getDepartment));
        dep.getStyleClass().add("profile-sub");
        depRow.getChildren().addAll(buildingIcon, dep);

        Label active = statusBadge(value(s, Employee::getEmploymentStatus).toUpperCase(), profileStatusClass(value(s, Employee::getEmploymentStatus)));
        person.getChildren().addAll(name, jobTitle, depRow, active);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox meta = new VBox(14);
        meta.setAlignment(Pos.CENTER_RIGHT);
        meta.getChildren().addAll(
                metaRow("/assets/icon_calendar_unique.png", "Fecha de ingreso", value(s, Employee::getStartDate)),
                metaRow("/assets/icon_link_unique.png", "Tipo de vinculación", value(s, Employee::getEmploymentStatus))
        );
        header.getChildren().addAll(avatarStack, person, spacer, meta);
        return new VBox(header);
    }

    private String profileStatusClass(String status) {
        if (status == null) return "status-approved";
        String e = status.toLowerCase();
        if (e.contains("retir") || e.contains("inactivo")) return "status-rejected";
        if (e.contains("encargo") || e.contains("provisional")) return "status-review";
        return "status-approved";
    }

    private Label statusBadge(String text, String styleClass) {
    String cleanText = text == null || text.isBlank() ? "NO REGISTRADO" : text.toUpperCase();

    Label badge = new Label(cleanText);
    badge.getStyleClass().add(styleClass);

    ImageView statusIcon = image(statusAssetPath(cleanText), 14, 14, true);

    if (statusIcon.getImage() == null) {
        StackPane assetSlot = new StackPane();
        assetSlot.getStyleClass().add("status-asset-slot");
        assetSlot.setMinSize(14, 14);
        assetSlot.setPrefSize(14, 14);
        assetSlot.setMaxSize(14, 14);
        badge.setGraphic(assetSlot);
    } else {
        badge.setGraphic(statusIcon);
    }

    badge.setGraphicTextGap(7);
    return badge;
}

private String statusAssetPath(String status) {
    if (status == null) return "/assets/status_default.png";

    String s = status.toLowerCase();

    if (s.contains("activo")) {
        return "/assets/status_activo.png";
    }

    if (s.contains("encargo")) {
        return "/assets/status_encargo.png";
    }

    if (s.contains("retirado") || s.contains("inactivo")) {
        return "/assets/status_retirado.png";
    }

    if (s.contains("provisional")) {
        return "/assets/status_provisional.png";
    }

    return "/assets/status_default.png";
}

    private HBox metaRow(String iconPath, String label, String val) {
        ImageView icon = image(iconPath, 25, 25, true);
        VBox texts = new VBox(3);
        Label l = new Label(label);
        l.getStyleClass().add("meta-label");
        Label v = new Label(val);
        v.getStyleClass().add("meta-value");
        texts.getChildren().addAll(l, v);
        return new HBox(10, icon, texts);
    }

    private VBox infoCard(String iconPath, String title, HBox... rows) {
        VBox card = new VBox(10);
        card.getStyleClass().add("info-card");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox heading = new HBox(8);
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getChildren().addAll(image(iconPath, 22, 22, true), sectionLabel(title));
        Separator sep = new Separator();
        sep.getStyleClass().add("gold-separator");
        sep.setPadding(new Insets(2, 0, 4, 0));
        card.getChildren().add(heading);
        card.getChildren().add(sep);
        card.getChildren().addAll(rows);
        return card;
    }

    private Label sectionLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("info-title");
        return label;
    }

    private HBox row(String label, String value) {
        HBox r = new HBox(8);
        r.getStyleClass().add("info-row");
        Label l = new Label(label);
        l.getStyleClass().add("info-label");
        String shownValue = value == null || value.isBlank() ? "NO REGISTRADO" : value;
        if (!shownValue.contains("@")) shownValue = upperUi(shownValue);
        Label v = new Label(shownValue);
        v.getStyleClass().add("info-value");
        v.setWrapText(true);
        v.setMaxWidth(200);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        r.getChildren().addAll(l, spacer, v);
        return r;
    }

    private VBox historyCard(Employee selected) {
        VBox history = new VBox(10);
        history.getStyleClass().add("info-card-wide");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        ImageView clockIcon = image("/assets/icon_history_unique.png", 22, 22, true);
        Label histTitle = sectionLabel("Historial reciente");
        titleRow.getChildren().addAll(clockIcon, histTitle);

        Separator sep = new Separator();
        sep.getStyleClass().add("gold-separator");

        TableView<Map<String, String>> historyTable = createHistoryTable(recentHistoryFor(selected));
        historyTable.setPrefHeight(240);
        history.getChildren().addAll(titleRow, sep, historyTable);
        return history;
    }

    private TableView<Map<String, String>> createHistoryTable(List<Map<String, String>> rows) {
        TableView<Map<String, String>> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(rows));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<String> keys = List.of("Realizado por", "Descripción", "Tipo de evento", "Fecha");
        for (String key : keys) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(key);
            col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrDefault(key, "")));

            if (key.equals("Tipo de evento")) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label(item.toUpperCase());
                        badge.getStyleClass().add(historyBadgeClass(item));
                        setGraphic(badge);
                        setText(null);
                    }
                });
            }

            col.setPrefWidth(switch (key) {
                case "Descripción" -> 330;
                case "Tipo de evento" -> 185;
                case "Realizado por" -> 220;
                default -> 170;
            });
            table.getColumns().add(col);
        }
        return table;
    }

    private List<Map<String, String>> recentHistoryFor(Employee selected) {
        if (selected == null) return List.of();
        String employmentStatus = value(selected, Employee::getEmploymentStatus);
        if (employmentStatus.toLowerCase().contains("retir") || employmentStatus.toLowerCase().contains("inactivo")) {
            return List.of();
        }

        String name = value(selected, Employee::getFullName).toUpperCase();
        int seed = Math.abs((value(selected, Employee::getRecordId) + name).hashCode());
        String[] types = {"ACTUALIZACIÓN DE DATOS", "DOCUMENTACIÓN", "VACACIONES", "ESTADO LABORAL", "CAMBIO DE DEPENDENCIA", "PERMISO"};
        String[] descs = {
                "ACTUALIZACIÓN DE INFORMACIÓN DE CONTACTO",
                "CARGA DE CERTIFICADO DE ESTUDIOS",
                "SOLICITUD DE VACACIONES APROBADA",
                "REVISIÓN DE SITUACIÓN ADMINISTRATIVA",
                "CAMBIO DE DEPENDENCIA REGISTRADO",
                "PERMISO ADMINISTRATIVO REGISTRADO"
        };
        String[] autores = {name, "TALENTO 360 HUMANO", "DIRECCIÓN DE TALENTO HUMANO"};
        List<Map<String, String>> rows = new ArrayList<>();
        int count = 4 + (seed % 3);
        for (int i = 0; i < count; i++) {
            int idx = (seed + i * 2) % types.length;
            rows.add(Map.of(
                    "Realizado por", autores[(seed + i) % autores.length],
                    "Descripción", descs[idx],
                    "Tipo de evento", types[idx],
                    "Fecha", String.format("%02d/%02d/2026  %02d:%02d %s",
                            1 + ((seed + i * 7) % 27),
                            1 + ((seed / 3 + i) % 5),
                            8 + ((seed + i) % 9),
                            (seed + i * 13) % 60,
                            i % 2 == 0 ? "A.M." : "P.M.")
            ));
        }
        return rows;
    }

    private String historyBadgeClass(String type) {
        String t = type == null ? "" : type.toLowerCase();
        if (t.contains("actualización") || t.contains("actualizacion") || t.contains("permiso")) return "tag-permiso";
        if (t.contains("dependencia") || t.contains("vacaciones")) return "tag-vacaciones";
        if (t.contains("document")) return "tag-incapacidad";
        return "tag-hoja";
    }

    private void setVacationsView() {
        setRequestsView("Vacaciones", "Vacaciones");
    }

    private void openRequestModule(String type) {
        switch (type == null ? "" : type) {
            case "Incapacidad" -> {
                setActive(disabilitiesButton);
                setRequestsView("Incapacidades", "Incapacidad");
            }
            case "Permiso" -> {
                setActive(permissionsButton);
                setRequestsView("Permisos", "Permiso");
            }
            case "Licencia maternidad" -> {
                setActive(maternityButton);
                setRequestsView("Licencia por maternidad", "Licencia maternidad");
            }
            default -> {
                setActive(vacationsButton);
                setRequestsView("Vacaciones", "Vacaciones");
            }
        }
    }

    private void setRequestsView(String pageTitle, String activeType) {
        VBox shell = pageShell();
        VBox content = new VBox(18);
        content.getStyleClass().add("page-content");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleTexts = new VBox(4);
        Label title = new Label(pageTitle);
        title.getStyleClass().add("hero-title");
        Label subtitle = new Label("Módulo específico para consultar, filtrar, registrar, editar estados y descargar soportes de " + pageTitle.toLowerCase() + ".");
        subtitle.getStyleClass().add("hero-subtitle");
        titleTexts.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button newBtn = new Button("  Nueva solicitud");
        ImageView plusIcon = image("/assets/icon_plus_unique.png", 17, 17, true);
        newBtn.setGraphic(plusIcon);
        newBtn.getStyleClass().add("green-button");
        titleRow.getChildren().addAll(titleTexts, spacer, newBtn);

        HBox summaryCards = new HBox(14);
        summaryCards.setMaxWidth(Double.MAX_VALUE);

        VBox filters = new VBox(10);
        filters.getStyleClass().add("filter-card");
        HBox filterTopRow = new HBox(14);
        filterTopRow.setAlignment(Pos.BOTTOM_LEFT);
        HBox filterBottomRow = new HBox(14);
        filterBottomRow.setAlignment(Pos.BOTTOM_LEFT);
        DatePicker start = datePicker("");
        DatePicker end = datePicker("");
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Todos", "Aprobada", "En revisión", "Finalizada", "Rechazada", "Pendiente"));
        status.setValue("Todos");
        status.getStyleClass().add("combo");
        ComboBox<String> departmentFilter = editableCombo(withDefault("Todas", departmentDAO.listDepartmentNames()), "Todas");
        departmentFilter.setValue("Todas");
        departmentFilter.setPrefWidth(230);
        ComboBox<String> jobTitleFilter = editableCombo(withDefault("Todos", departmentDAO.listJobTitleNames()), "Todos");
        jobTitleFilter.setValue("Todos");
        jobTitleFilter.setPrefWidth(210);
        TextField search = filterField("Buscar radicado, servidor o dependencia...");
        Button requestSearchButton = new Button("Buscar");
        requestSearchButton.getStyleClass().add("primary-button");
        Button clearFiltersButton = new Button("Limpiar");
        clearFiltersButton.getStyleClass().add("secondary-button");
        filterTopRow.getChildren().addAll(
                filterBox("Fecha inicio", start),
                filterBox("Fecha fin", end),
                filterBox("Estado", status),
                filterBox("Dependencia", departmentFilter),
                filterBox("Cargo", jobTitleFilter)
        );
        filterBottomRow.getChildren().addAll(
                filterBox("Buscar", search),
                requestSearchButton,
                clearFiltersButton
        );
        filters.getChildren().addAll(filterTopRow, filterBottomRow);

        TableView<AdministrativeRequest> table = createRequestsTable();
        Label footer = new Label();
        footer.getStyleClass().add("pagination");
        footer.setPadding(new Insets(4, 0, 0, 0));

        HBox tableFooter = new HBox(8);
        tableFooter.setAlignment(Pos.CENTER_LEFT);
        Region tfSpacer = new Region();
        HBox.setHgrow(tfSpacer, Priority.ALWAYS);
        HBox pageBox = new HBox(4);
        pageBox.setAlignment(Pos.CENTER_RIGHT);
        Label prev = new Label("‹");
        prev.getStyleClass().add("pagination-arrow");
        Button one = pageBtn("1", true);
        Button two = pageBtn("2", false);
        Button three = pageBtn("3", false);
        List<Button> pageButtons = List.of(one, two, three);
        Label next = new Label("›");
        next.getStyleClass().add("pagination-arrow");
        pageBox.getChildren().addAll(prev, one, two, three, next);
        tableFooter.getChildren().addAll(footer, tfSpacer, pageBox);

        final int pageSize = 20;
        final int[] currentPage = {1};
        final int[] totalPages = {1};
        final List<AdministrativeRequest>[] currentData = new List[]{new ArrayList<>()};

        Runnable renderPage = () -> {
            int total = currentData[0].size();
            totalPages[0] = Math.max(1, (int) Math.ceil(total / (double) pageSize));
            currentPage[0] = Math.max(1, Math.min(currentPage[0], totalPages[0]));
            List<AdministrativeRequest> pageRows = pageSliceRequests(currentData[0], currentPage[0], pageSize);
            table.setItems(FXCollections.observableArrayList(pageRows));
            int from = total == 0 ? 0 : ((currentPage[0] - 1) * pageSize) + 1;
            int to = Math.min(currentPage[0] * pageSize, total);
            footer.setText("Mostrando " + from + " a " + to + " de " + total + " registros visibles");
            updateRequestPageButtons(pageButtons, currentPage[0], totalPages[0]);
        };

        Runnable refresh = () -> {
    String filter = search.getText() == null ? "" : search.getText().trim().toLowerCase();

    List<AdministrativeRequest> data = requestDAO.list("").stream()
            .filter(v -> recordType(v).equals(activeType))
            .filter(v -> !"Licencia maternidad".equals(activeType) || isFemaleRequest(v))
            .filter(v -> status.getValue() == null || status.getValue().equals("Todos") || recordStatus(v).equals(status.getValue()))
            .filter(v -> matchesComboFilter(v.getDepartment(), comboText(departmentFilter), "Todas"))
            .filter(v -> matchesComboFilter(v.getJobTitle(), comboText(jobTitleFilter), "Todos"))
            .filter(v -> matchesDateRange(v, start.getValue(), end.getValue()))
            .filter(v -> matchesRequestSearch(v, filter))
            .collect(Collectors.toList());

    currentData[0] = data;
    currentPage[0] = 1;
    fillRequestSummaryCards(summaryCards, activeType, data);
    renderPage.run();
};

        activeRequestsRefresh = refresh;
        activeRequestsReset = () -> {
            start.setValue(null);
            end.setValue(null);
            status.setValue("Todos");
            setComboValue(departmentFilter, "Todas");
            setComboValue(jobTitleFilter, "Todos");
            search.clear();
            refresh.run();
        };

        requestSearchButton.setOnAction(e -> refresh.run());
        clearFiltersButton.setOnAction(e -> activeRequestsReset.run());
        search.setOnAction(e -> refresh.run());
        status.setOnAction(e -> refresh.run());
        departmentFilter.setOnAction(e -> refresh.run());
        jobTitleFilter.setOnAction(e -> refresh.run());
        start.setOnAction(e -> refresh.run());
        end.setOnAction(e -> refresh.run());
        for (Button pageButton : pageButtons) {
            pageButton.setOnAction(e -> {
                try {
                    int page = Integer.parseInt(pageButton.getText());
                    if (page >= 1 && page <= totalPages[0]) {
                        currentPage[0] = page;
                        renderPage.run();
                    }
                } catch (NumberFormatException ignored) {}
            });
        }
        prev.setOnMouseClicked(e -> {
            if (currentPage[0] > 1) {
                currentPage[0]--;
                renderPage.run();
            }
        });
        next.setOnMouseClicked(e -> {
            if (currentPage[0] < totalPages[0]) {
                currentPage[0]++;
                renderPage.run();
            }
        });
        newBtn.setOnAction(e -> {
            if (!authDAO.canCreateRequests()) {
                showAlert("Permisos insuficientes", "Tu usuario tiene rol de " + authDAO.getCurrentRole() + ". Puedes consultar y descargar, pero no crear solicitudes.");
                return;
            }
            showNewRequestDialog(table, activeType);
        });
        refresh.run();

        VBox card = new VBox(12, table, tableFooter);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(titleRow, summaryCards, filters, card, footerBar());
        shell.getChildren().add(content);
        setCenterPage(shell);
    }

    private void fillRequestSummaryCards(HBox cards, String activeType, List<AdministrativeRequest> data) {
        long pendientes = data.stream().filter(v -> "Pendiente".equals(recordStatus(v))).count();
        long revision = data.stream().filter(v -> "En revisión".equals(recordStatus(v))).count();
        long aprobadas = data.stream().filter(v -> "Aprobada".equals(recordStatus(v)) || "Finalizada".equals(recordStatus(v))).count();
        cards.getChildren().clear();
        Node c1 = coloredStatCard(iconForType(activeType), "icon-bg-teal", "Total del módulo", format(data.size()), "Registros visibles", "trend-neutral");
        Node c2 = coloredStatCard("/assets/icon_search_unique.png", "icon-bg-blue", "En revisión", format((int) revision), "Requieren validación", "trend-neutral");
        Node c3 = coloredStatCard("/assets/status_pending.png", "icon-bg-orange", "Pendientes", format((int) pendientes), "Por gestionar", "trend-neutral");
        Node c4 = coloredStatCard("/assets/status_approved.png", "icon-bg-green", "Aprobadas/finalizadas", format((int) aprobadas), "Procesadas", "trend-label");
        for (Node c : List.of(c1, c2, c3, c4)) HBox.setHgrow(c, Priority.ALWAYS);
        cards.getChildren().addAll(c1, c2, c3, c4);
    }

    private HBox requestSummaryCards(String activeType) {
        List<AdministrativeRequest> data = requestDAO.list("").stream()
                .filter(v -> recordType(v).equals(activeType))
                .filter(v -> !"Licencia maternidad".equals(activeType) || isFemaleRequest(v))
                .collect(Collectors.toList());
        HBox cards = new HBox(14);
        cards.setMaxWidth(Double.MAX_VALUE);
        fillRequestSummaryCards(cards, activeType, data);
        return cards;
    }

    private boolean matchesRequestSearch(AdministrativeRequest v, String filter) {
    if (filter == null || filter.isBlank()) return true;

    return containsSearch(filingNumber(v), filter)
            || containsSearch(recordType(v), filter)
            || containsSearch(recordStatus(v), filter)
            || containsSearch(v.getPerson(), filter)
            || containsSearch(v.getDocument(), filter)
            || containsSearch(v.getDepartment(), filter)
            || containsSearch(v.getJobTitle(), filter)
            || containsSearch(v.getStartDate(), filter)
            || containsSearch(v.getTotalDays(), filter)
            || containsSearch(v.getNotes(), filter)
            || containsSearch(v.getRequestType(), filter);
}

private boolean containsSearch(String value, String filter) {
    return value != null && value.toLowerCase().contains(filter);
}

    private boolean matchesComboFilter(String value, String selected, String allValue) {
        if (selected == null || selected.isBlank() || normalizeLookup(selected).equals(normalizeLookup(allValue))) return true;
        return value != null && normalizeLookup(value).contains(normalizeLookup(selected));
    }

    private boolean matchesDateRange(AdministrativeRequest v, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        LocalDate date = parseUiDate(v == null ? null : v.getStartDate());
        if (date == null) return false;
        if (start != null && date.isBefore(start)) return false;
        return end == null || !date.isAfter(end);
    }

    private List<AdministrativeRequest> pageSliceRequests(List<AdministrativeRequest> rows, int page, int pageSize) {
        if (rows == null || rows.isEmpty()) return List.of();
        int safePage = Math.max(1, page);
        int from = Math.min((safePage - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());
        return new ArrayList<>(rows.subList(from, to));
    }

    private void updateRequestPageButtons(List<Button> buttons, int currentPage, int totalPages) {
        int start = Math.max(1, Math.min(currentPage, Math.max(1, totalPages - buttons.size() + 1)));
        for (int i = 0; i < buttons.size(); i++) {
            int pageNumber = start + i;
            Button btn = buttons.get(i);
            btn.setText(String.valueOf(pageNumber));
            btn.setDisable(pageNumber > totalPages);
            btn.getStyleClass().removeAll("page-btn", "page-btn-inactive");
            btn.getStyleClass().add(pageNumber == currentPage ? "page-btn" : "page-btn-inactive");
        }
    }

    private boolean isFemaleRequest(AdministrativeRequest v) {
        String person = v == null || v.getPerson() == null ? "" : v.getPerson().toLowerCase();
        String noteText = v == null || v.getNotes() == null ? "" : v.getNotes().toLowerCase();
        String combined = person + " " + noteText;
        if (combined.contains(" genero: f") || combined.contains(" género: f") || combined.contains("mujer") || combined.contains("femenino")) return true;
        String[] maleNames = {" carlos", " juan", " miguel", " andrés", "andres", " felipe", " jorge", " santiago", " daniel", " camilo", " sebastián", "sebastian", "eduardo", "pablo"};
        for (String male : maleNames) {
            if ((" " + person + " ").contains(male + " ")) return false;
        }
        String[] femaleNames = {" laura", " diana", " paula", " natalia", " maría", " maria", " valentina", " mónica", " monica", " claudia", " carolina", " isabella", " lucía", " lucia", " camila", " fernanda", " alejandra", " verónica", " veronica", " dahiana", " ana", " sofía", " sofia"};
        for (String female : femaleNames) {
            if ((" " + person + " ").contains(female + " ")) return true;
        }
        return false;
    }

    private String iconForType(String type) {
        return switch (type) {
            case "Incapacidad" -> "/assets/icon_incapacity_unique_two.png";
            case "Permiso" -> "/assets/icon_file_unique.png";
            case "Licencia maternidad" -> "/assets/icon_maternity_unique.png";
            default -> "/assets/icon_vacations_unique.png";
        };
    }

    private ImageView vacTabIcon(String path) {
        return image(path, 22, 22, true);
    }

    private void setTabStyles(Button active, Button... inactiveButtons) {
        active.getStyleClass().removeAll("tab", "tab-active");
        active.getStyleClass().add("tab-active");
        for (Button inactive : inactiveButtons) {
            inactive.getStyleClass().removeAll("tab", "tab-active");
            inactive.getStyleClass().add("tab");
        }
    }

    private TableView<AdministrativeRequest> createRequestsTable() {
        TableView<AdministrativeRequest> table = new TableView<>();
        table.getStyleClass().add("requests-table");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(680);
        table.setMinHeight(560);

        addColumn(table, "Radicado", this::filingNumber, 160);
        addColumn(table, "Tipo", this::recordType, 165);
        addColumn(table, "Servidor", AdministrativeRequest::getPerson, 280);
        addColumn(table, "Documento", AdministrativeRequest::getDocument, 140);
        addColumn(table, "Dependencia", AdministrativeRequest::getDepartment, 270);
        addColumn(table, "Cargo", AdministrativeRequest::getJobTitle, 250);
        addColumn(table, "Fecha inicio", AdministrativeRequest::getStartDate, 140);
        addColumn(table, "Fecha fin", this::endDate, 135);
        addColumn(table, "Días", AdministrativeRequest::getTotalDays, 85);

        TableColumn<AdministrativeRequest, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(recordStatus(cell.getValue())));
        statusColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label text = new Label(item);
                HBox badge = new HBox(6, image(requestStatusAsset(item), 13, 13, true), text);
                badge.setAlignment(Pos.CENTER);
                badge.getStyleClass().add(requestBadgeClass(item));
                setGraphic(badge);
                setText(null);
            }
        });
        statusColumn.setPrefWidth(155);
        table.getColumns().add(statusColumn);

        addColumn(table, "Fecha solicitud", v -> requestDate(v), 155);

        TableColumn<AdministrativeRequest, String> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setCellValueFactory(cell -> new SimpleStringProperty(""));
        accionesCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                AdministrativeRequest row = getTableView().getItems().get(getIndex());
                HBox btns = new HBox(8);
                btns.setAlignment(Pos.CENTER);
                Button eye = actionIconButton("/assets/icon_view.png", "Ver detalle");
                eye.setOnAction(e -> showRequestDetails(row));
                Button edit = actionIconButton("/assets/icon_file.png", authDAO.canEditRequests() ? "Gestionar solicitud" : "Solo lectura");
                edit.setOnAction(e -> showManageRequestDialog(row, getTableView()));
                Button dl = actionIconButton("/assets/icon_download_unique.png", "Descargar soporte");
                dl.setOnAction(e -> downloadRequest(row));
                Button delete = actionIconButton("/assets/status_denied.png", authDAO.canEditRequests() ? "Eliminar solicitud" : "Solo lectura");
                delete.setOnAction(e -> showDeleteRequestDialog(row));
                btns.getChildren().addAll(eye, edit, dl, delete);
                setGraphic(btns);
                setText(null);
            }
        });
        accionesCol.setPrefWidth(178);
        table.getColumns().add(accionesCol);
        return table;
    }

    private String requestStatusAsset(String status) {
        return switch (normalizeRequestStatus(status)) {
            case "Aprobada" -> "/assets/status_approved.png";
            case "Finalizada" -> "/assets/status_finalized.png";
            case "Rechazada" -> "/assets/status_denied.png";
            case "En revisión" -> "/assets/status_revision.png";
            default -> "/assets/status_pending.png";
        };
    }

    private String requestBadgeClass(String status) {
        return switch (normalizeRequestStatus(status)) {
            case "Aprobada" -> "badge-aprobada";
            case "Finalizada" -> "badge-finalizada";
            case "Rechazada" -> "badge-rechazada";
            case "En revisión" -> "badge-revision";
            default -> "badge-pendiente";
        };
    }

    private void loadRequestsTable(TableView<AdministrativeRequest> table, Label footer, String filter, String tab, String status, String type) {
        List<AdministrativeRequest> data = requestDAO.list(filter).stream()
                .filter(v -> recordType(v).equals(tab))
                .filter(v -> status == null || status.equals("Todos") || recordStatus(v).equals(status))
                .filter(v -> type == null || type.equals("Todos") || recordType(v).equals(type))
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(data));
        footer.setText("Mostrando " + (data.isEmpty() ? 0 : 1) + " a " + data.size() + " de " + data.size() + " registros");
    }

    private TextField filterField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input");
        field.setPrefWidth(155);
        return field;
    }

    private DatePicker datePicker(String prompt) {
        DatePicker picker = new DatePicker();
        picker.setPromptText(prompt);
        picker.getStyleClass().add("input");
        picker.setPrefWidth(155);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(UI_DATE);
            }

            @Override
            public LocalDate fromString(String value) {
                return parseUiDate(value);
            }
        });
        return picker;
    }

    private VBox filterBox(String label, Control control) {
        VBox box = new VBox(5);
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        box.getChildren().addAll(l, control);
        return box;
    }

    private void showNewRequestDialog(TableView<AdministrativeRequest> table, String defaultType) {
        if (appFrame == null) return;

        VBox card = new VBox(14);
        card.getStyleClass().add("modal-card-large");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_new_form.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box");
        VBox headerTexts = new VBox(3);
        Label title = new Label("Nueva solicitud");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Formulario compacto con datos administrativos, estado inicial y observaciones internas.");
        subtitle.getStyleClass().add("modal-subtitle");
        headerTexts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, headerTexts);

        GridPane form = new GridPane();
        form.getStyleClass().add("decorated-form");
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        List<Employee> employees = employeeDAO.list("").stream()
                .filter(s -> s.getFullName() != null && !s.getFullName().isBlank())
                .sorted(Comparator.comparing(s -> s.getFullName().toLowerCase()))
                .collect(Collectors.toList());
        Map<String, Employee> employeeByName = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        for (Employee employee : employees) {
            String name = upperUi(employee.getFullName());
            String key = normalizeLookup(name);
            if (!employeeByName.containsKey(key)) {
                employeeByName.put(key, employee);
                names.add(name);
            }
        }

        ComboBox<String> person = editableCombo(names, "Nombre completo");
        TextField document = decoratedTextField("Documento");
        ComboBox<String> department = editableCombo(departmentDAO.listDepartmentNames(), "Dependencia");
        ComboBox<String> jobTitle = editableCombo(departmentDAO.listJobTitleNames(), "Cargo");
        DatePicker date = datePicker("Fecha inicio");
        date.setPrefWidth(260);
        TextField days = decoratedTextField("Días solicitados");
        TextArea notes = new TextArea();
        notes.setPromptText("Observaciones, soporte o comentario interno...");
        notes.getStyleClass().add("form-text-area");
        notes.setPrefRowCount(3);
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Vacaciones", "Incapacidad", "Permiso", "Licencia maternidad"));
        type.setValue(defaultType == null || defaultType.isBlank() ? "Vacaciones" : defaultType);
        type.getStyleClass().add("combo");
        type.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> initialStatus = new ComboBox<>(FXCollections.observableArrayList("Pendiente", "En revisión", "Aprobada", "Finalizada", "Rechazada"));
        initialStatus.setValue("Pendiente");
        initialStatus.getStyleClass().add("combo");
        initialStatus.setMaxWidth(Double.MAX_VALUE);

        final String[] lastSuggestedDays = {defaultDaysForType(type.getValue())};
        days.setText(lastSuggestedDays[0]);
        type.setOnAction(e -> {
            String suggested = defaultDaysForType(type.getValue());
            if (days.getText() == null || days.getText().isBlank() || days.getText().trim().equals(lastSuggestedDays[0])) {
                days.setText(suggested);
            }
            lastSuggestedDays[0] = suggested;
        });

        Runnable completeEmployee = () -> populateRequestEmployee(comboText(person), employeeByName, document, department, jobTitle);
        person.setOnAction(e -> completeEmployee.run());
        person.getEditor().focusedProperty().addListener((noteText, wasFocused, focused) -> {
            if (!focused) completeEmployee.run();
        });

        form.add(formField("Servidor", person), 0, 0);
        form.add(formField("Documento", document), 1, 0);
        form.add(formField("Dependencia", department), 0, 1);
        form.add(formField("Cargo", jobTitle), 1, 1);
        form.add(formField("Fecha inicio", date), 0, 2);
        form.add(formField("Días", days), 1, 2);
        form.add(formField("Tipo", type), 0, 3);
        form.add(formField("Estado inicial", initialStatus), 1, 3);
        form.add(formField("Observaciones", notes), 0, 4, 2, 1);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button save = new Button("Guardar solicitud");
        save.getStyleClass().add("green-button");
        actions.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, form, actions);
        StackPane overlay = modalOverlay(card);

        cancel.setOnAction(e -> closeOverlay(overlay));
        save.setOnAction(e -> {
            if (comboText(person).isBlank() || document.getText().isBlank() || comboText(department).isBlank() || date.getValue() == null) {
                showAlert("Formulario incompleto", "Completa mínimo servidor, documento y dependencia.\n\nEl sistema necesita esos datos para crear el radicado y ubicar la solicitud dentro del módulo correcto.");
                return;
            }
            String noteText = "Tipo: " + type.getValue() + " | Estado inicial: " + initialStatus.getValue() + (notes.getText().isBlank() ? "" : " | " + notes.getText());
            int daysCount = parsePositiveDays(days.getText());
            if (daysCount <= 0) {
                showAlert("Dias invalidos", "Ingresa un numero de dias mayor a cero.");
                return;
            }
            Employee selected = employeeByName.get(normalizeLookup(comboText(person)));
            String gender = selected == null ? "" : selected.getGender();
            if (gender != null && !gender.isBlank()) noteText += " | Genero: " + gender;
            AdministrativeRequest v = new AdministrativeRequest(0, upperUi(comboText(department)), upperUi(comboText(person)), document.getText(), upperUi(comboText(jobTitle)),
                    formatUiDate(date.getValue()), String.valueOf(daysCount), "Periodo actual", noteText, type.getValue(), initialStatus.getValue(), formatUiDate(LocalDate.now()));
            if ("Licencia maternidad".equals(type.getValue()) && !isFemaleRequest(v)) {
                showAlert("Validación de maternidad", "La licencia por maternidad debe registrarse únicamente para servidoras. Revisa el nombre ingresado antes de guardar.");
                return;
            }
            int newId = requestDAO.createReturningId(v);
            if (newId > 0) {
                closeOverlay(overlay);
                AdministrativeRequest persisted = new AdministrativeRequest(newId, v.getDepartment(), v.getPerson(), v.getDocument(), v.getJobTitle(),
                        v.getStartDate(), v.getTotalDays(), v.getPeriods(), v.getNotes(), v.getRequestType(), v.getStatus(), v.getRequestDate());
                openRequestModule(recordType(persisted));
                showAlert("Solicitud registrada", "La solicitud fue guardada correctamente en PostgreSQL.\n\nRadicado: " + filingNumber(persisted)
                        + "\nTipo: " + type.getValue()
                        + "\nEstado inicial: " + initialStatus.getValue()
                        + "\nServidor: " + comboText(person)
                        + "\nDependencia: " + comboText(department));
            } else {
                showAlert("No se pudo guardar", "No se pudo guardar la solicitud en PostgreSQL.\n\nRevisa que la base de datos talento360 esté activa y que el script final haya sido ejecutado.");
            }
        });
    }

    private TextField decoratedTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private ComboBox<String> editableCombo(List<String> values, String prompt) {
        Set<String> seen = new HashSet<>();
        List<String> cleanValues = values == null ? List.of() : values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(this::upperUi)
                .filter(v -> seen.add(normalizeLookup(v)))
                .sorted()
                .collect(Collectors.toList());
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(cleanValues));
        combo.setEditable(true);
        combo.setPromptText(prompt);
        combo.getStyleClass().add("combo");
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private List<String> withDefault(String defaultValue, List<String> values) {
        List<String> result = new ArrayList<>();
        result.add(defaultValue);
        if (values != null) {
            values.stream()
                    .filter(v -> v != null && !v.isBlank())
                    .filter(v -> !normalizeLookup(v).equals(normalizeLookup(defaultValue)))
                    .forEach(result::add);
        }
        return result;
    }

    private String comboText(ComboBox<String> combo) {
        if (combo == null) return "";
        String editorText = combo.isEditable() && combo.getEditor() != null ? combo.getEditor().getText() : null;
        String value = editorText != null && !editorText.isBlank() ? editorText : combo.getValue();
        return value == null ? "" : value.trim();
    }

    private void populateRequestEmployee(String name, Map<String, Employee> employeeByName, TextField document,
                                           ComboBox<String> department, ComboBox<String> jobTitle) {
        Employee employee = employeeByName.get(normalizeLookup(name));
        if (employee == null) return;
        document.setText(employee.getDocumentId() == null ? "" : employee.getDocumentId());
        setComboValue(department, employee.getDepartment());
        setComboValue(jobTitle, employee.getCurrentJobTitle());
    }

    private void setComboValue(ComboBox<String> combo, String value) {
        if (combo == null || value == null || value.isBlank()) return;
        String clean = upperUi(value);
        if (!combo.getItems().contains(clean)) combo.getItems().add(clean);
        combo.setValue(clean);
        if (combo.isEditable() && combo.getEditor() != null) combo.getEditor().setText(clean);
    }

    private String normalizeLookup(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String defaultDaysForType(String type) {
        return switch (type == null ? "" : type) {
            case "Incapacidad" -> "3";
            case "Permiso" -> "1";
            case "Licencia maternidad" -> "126";
            default -> "15";
        };
    }

    private int parsePositiveDays(String value) {
        try {
            int days = Integer.parseInt(value == null ? "" : value.trim());
            return days > 0 ? days : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private VBox formField(String label, Control control) {
        VBox box = new VBox(6);
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        control.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(l, control);
        return box;
    }

    private <T> void addColumn(TableView<T> table, String title, java.util.function.Function<T, String> getter, int width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        column.setPrefWidth(width);
        table.getColumns().add(column);
    }

    private void exportMapTable(TableView<Map<String, String>> table) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Excel", "*.xlsx"));
        chooser.setInitialFileName("registros_recientes.xlsx");
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        if (table.getItems().isEmpty()) {
            showAlert("Sin datos", "No hay registros para exportar.");
            return;
        }
        try {
            List<String> headers = new ArrayList<>(table.getItems().get(0).keySet());
            writeSimpleXlsx(file, headers, table.getItems());
            showAlert("Exportación lista", "El archivo Excel fue creado correctamente.");
        } catch (Exception e) {
            showAlert("Error", "No se pudo exportar: " + e.getMessage());
        }
    }

    private void writeSimpleXlsx(File file, List<String> headers, List<Map<String, String>> rows) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            putZip(zip, "[Content_Types].xml", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                    <Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">
                      <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>
                      <Default Extension=\"xml\" ContentType=\"application/xml\"/>
                      <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>
                      <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>
                    </Types>
                    """);
            putZip(zip, "_rels/.rels", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                    <Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">
                      <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>
                    </Relationships>
                    """);
            putZip(zip, "xl/workbook.xml", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                    <workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">
                      <sheets><sheet name=\"Registros\" sheetId=\"1\" r:id=\"rId1\"/></sheets>
                    </workbook>
                    """);
            putZip(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                    <Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">
                      <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>
                    </Relationships>
                    """);

            StringBuilder sheet = new StringBuilder();
            sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sheet.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
            appendRow(sheet, 1, headers);
            int r = 2;
            for (Map<String, String> row : rows) {
                List<String> values = headers.stream().map(h -> row.getOrDefault(h, "")).toList();
                appendRow(sheet, r++, values);
            }
            sheet.append("</sheetData></worksheet>");
            putZip(zip, "xl/worksheets/sheet1.xml", sheet.toString());
        }
    }

    private void appendRow(StringBuilder sheet, int rowIndex, List<String> values) {
        sheet.append("<row r=\"").append(rowIndex).append("\">");
        for (int i = 0; i < values.size(); i++) {
            String col = excelCol(i + 1);
            sheet.append("<c r=\"").append(col).append(rowIndex).append("\" t=\"inlineStr\"><is><t>")
                    .append(xml(values.get(i))).append("</t></is></c>");
        }
        sheet.append("</row>");
    }

    private String excelCol(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            n--;
            sb.insert(0, (char)('A' + (n % 26)));
            n /= 26;
        }
        return sb.toString();
    }

    private void putZip(ZipOutputStream zip, String name, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.strip().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String xml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private Button actionIconButton(String asset, String tooltip) {
        Button b = new Button();
        b.getStyleClass().add("action-icon-button");
        b.setGraphic(image(asset, 16, 16, true));
        b.setTooltip(new Tooltip(tooltip));
        return b;
    }

    private void showDeleteRequestDialog(AdministrativeRequest v) {
        if (!authDAO.canEditRequests()) {
            showAlert("Solo lectura", "Tu usuario tiene rol de " + authDAO.getCurrentRole() + ".\n\nPuedes revisar detalles y descargar soportes, pero no eliminar solicitudes.");
            return;
        }
        if (v == null || v.getRequestId() <= 0) {
            showAlert("No se puede eliminar", "Esta solicitud pertenece a los datos demo y no tiene un registro real asociado en PostgreSQL.");
            return;
        }

        VBox card = new VBox(16);
        card.getStyleClass().add("modal-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/status_denied.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box-danger");
        VBox texts = new VBox(3);
        Label title = new Label("Eliminar solicitud");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label(filingNumber(v) + " - " + recordType(v));
        subtitle.getStyleClass().add("modal-subtitle");
        texts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, texts);

        Label detail = new Label("La solicitud se eliminara de la tabla vacaciones y tambien del historial asociado, si existe.");
        detail.getStyleClass().add("modal-message");
        detail.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button delete = new Button("Eliminar");
        delete.getStyleClass().add("danger-button");
        actions.getChildren().addAll(cancel, delete);

        card.getChildren().addAll(header, detail, actions);
        StackPane overlay = modalOverlay(card);
        cancel.setOnAction(e -> closeOverlay(overlay));
        delete.setOnAction(e -> {
            if (requestDAO.delete(v.getRequestId())) {
                statusOverrides.remove(v.getRequestId());
                closeOverlay(overlay);
                activeRequestsRefresh.run();
                showAlert("Solicitud eliminada", "La solicitud " + filingNumber(v) + " fue eliminada correctamente de PostgreSQL.");
            } else {
                showAlert("No se pudo eliminar", "No se pudo eliminar la solicitud en PostgreSQL.\n\nRevisa la conexion y que el registro exista en la tabla vacaciones.");
            }
        });
    }

    private void showManageRequestDialog(AdministrativeRequest v, TableView<AdministrativeRequest> table) {
        if (!authDAO.canEditRequests()) {
            showAlert("Solo lectura", "Tu usuario tiene rol de " + authDAO.getCurrentRole() + ".\n\nPuedes revisar detalles y descargar soportes, pero no cambiar el estado de las solicitudes.");
            return;
        }

        VBox card = new VBox(14);
        card.getStyleClass().add("modal-card-large");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_file.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box");
        VBox texts = new VBox(3);
        Label title = new Label("Gestionar solicitud");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label(filingNumber(v) + " - " + recordType(v));
        subtitle.getStyleClass().add("modal-subtitle");
        texts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, texts);

        List<Employee> employees = employeeDAO.list("").stream()
                .filter(s -> s.getFullName() != null && !s.getFullName().isBlank())
                .sorted(Comparator.comparing(s -> s.getFullName().toLowerCase()))
                .collect(Collectors.toList());
        Map<String, Employee> employeeByName = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        for (Employee employee : employees) {
            String name = upperUi(employee.getFullName());
            String key = normalizeLookup(name);
            if (!employeeByName.containsKey(key)) {
                employeeByName.put(key, employee);
                names.add(name);
            }
        }

        GridPane form = new GridPane();
        form.getStyleClass().add("decorated-form");
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        ComboBox<String> person = editableCombo(names, "Nombre completo");
        setComboValue(person, v.getPerson());
        TextField document = decoratedTextField("Documento");
        document.setText(v.getDocument() == null ? "" : v.getDocument());
        ComboBox<String> department = editableCombo(departmentDAO.listDepartmentNames(), "Dependencia");
        setComboValue(department, v.getDepartment());
        ComboBox<String> jobTitle = editableCombo(departmentDAO.listJobTitleNames(), "Cargo");
        setComboValue(jobTitle, v.getJobTitle());
        DatePicker startDate = datePicker("Fecha inicio");
        startDate.setValue(parseUiDate(v.getStartDate()));
        TextField days = decoratedTextField("Días solicitados");
        days.setText(v.getTotalDays() == null ? "" : v.getTotalDays());
        TextField periods = decoratedTextField("Periodo");
        periods.setText(v.getPeriods() == null ? "Periodo actual" : v.getPeriods());
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Vacaciones", "Incapacidad", "Permiso", "Licencia maternidad"));
        type.setValue(recordType(v));
        type.getStyleClass().add("combo");
        type.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Aprobada", "En revisión", "Finalizada", "Rechazada", "Pendiente"));
        status.setValue(recordStatus(v));
        status.getStyleClass().add("combo");
        status.setMaxWidth(Double.MAX_VALUE);
        TextArea notes = new TextArea();
        notes.setPromptText("Observaciones, soporte o comentario interno...");
        notes.setText(v.getNotes() == null ? "" : v.getNotes());
        notes.getStyleClass().add("form-text-area");
        notes.setPrefRowCount(3);
        TextArea managementNote = new TextArea();
        managementNote.setPromptText("Nota interna de gestión o revisión...");
        managementNote.getStyleClass().add("form-text-area");
        managementNote.setPrefRowCount(2);

        Runnable completeEmployee = () -> populateRequestEmployee(comboText(person), employeeByName, document, department, jobTitle);
        person.setOnAction(e -> completeEmployee.run());
        person.getEditor().focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (!focused) completeEmployee.run();
        });

        form.add(formField("Servidor", person), 0, 0);
        form.add(formField("Documento", document), 1, 0);
        form.add(formField("Dependencia", department), 0, 1);
        form.add(formField("Cargo", jobTitle), 1, 1);
        form.add(formField("Fecha inicio", startDate), 0, 2);
        form.add(formField("Días", days), 1, 2);
        form.add(formField("Periodo", periods), 0, 3);
        form.add(formField("Tipo", type), 1, 3);
        form.add(formField("Estado", status), 0, 4);
        form.add(formField("Observaciones", notes), 0, 5, 2, 1);
        form.add(formField("Nota de gestión", managementNote), 0, 6, 2, 1);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button save = new Button("Guardar cambios");
        save.getStyleClass().add("green-button");
        actions.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, form, actions);
        StackPane overlay = modalOverlay(card);
        cancel.setOnAction(e -> closeOverlay(overlay));
        save.setOnAction(e -> {
            String personText = comboText(person);
            String departmentText = comboText(department);
            String jobTitleText = comboText(jobTitle);
            String startDateText = formatUiDate(startDate.getValue());
            int daysCount = parsePositiveDays(days.getText());
            if (personText.isBlank() || document.getText().isBlank() || departmentText.isBlank() || startDateText.isBlank()) {
                showAlert("Formulario incompleto", "Completa servidor, documento, dependencia y fecha de inicio.");
                return;
            }
            if (daysCount <= 0) {
                showAlert("Días inválidos", "Ingresa un número de días mayor a cero.");
                return;
            }
            Employee selected = employeeByName.get(normalizeLookup(personText));
            String gender = selected == null ? "" : selected.getGender();
            String noteText = notes.getText() == null ? "" : notes.getText().trim();
            if (gender != null && !gender.isBlank() && !normalizeLookup(noteText).contains("genero:")) {
                noteText += (noteText.isBlank() ? "" : " | ") + "Genero: " + gender;
            }
            AdministrativeRequest updated = new AdministrativeRequest(
                    v.getRequestId(),
                    upperUi(departmentText),
                    upperUi(personText),
                    document.getText().trim(),
                    upperUi(jobTitleText),
                    startDateText,
                    String.valueOf(daysCount),
                    periods.getText() == null || periods.getText().isBlank() ? "Periodo actual" : periods.getText().trim(),
                    noteText,
                    type.getValue(),
                    status.getValue(),
                    v.getRequestDate()
            );
            if ("Licencia maternidad".equals(type.getValue()) && !isFemaleRequest(updated)) {
                showAlert("Validación de maternidad", "La licencia por maternidad debe registrarse únicamente para servidoras. Revisa el servidor seleccionado antes de guardar.");
                return;
            }
            if (requestDAO.update(updated, managementNote.getText())) {
                statusOverrides.put(v.getRequestId(), normalizeRequestStatus(status.getValue()));
                closeOverlay(overlay);
                openRequestModule(recordType(updated));
                showAlert("Solicitud actualizada", "La solicitud " + filingNumber(updated) + " fue actualizada correctamente en PostgreSQL.");
            } else {
                showAlert("No se pudo actualizar", "No se pudo guardar la solicitud en PostgreSQL.\n\nRevisa la conexión y la tabla vacaciones.");
            }
        });
    }

    private TextField readonlyField(String value) {
        TextField field = decoratedTextField("");
        field.setText(value == null ? "" : value);
        field.setEditable(false);
        field.getStyleClass().add("readonly-field");
        return field;
    }

    private void showRequestDetails(AdministrativeRequest v) {
        showAlert("Detalle de solicitud", "Radicado: " + filingNumber(v)
                + "\nTipo: " + recordType(v)
                + "\nEstado actual: " + recordStatus(v)
                + "\n\nServidor: " + v.getPerson()
                + "\nDocumento: " + v.getDocument()
                + "\nDependencia: " + v.getDepartment()
                + "\nCargo: " + v.getJobTitle()
                + "\n\nFecha inicio: " + v.getStartDate()
                + "\nFecha fin estimada: " + endDate(v)
                + "\nDías solicitados: " + v.getTotalDays()
                + "\nPeriodo: " + (v.getPeriods() == null || v.getPeriods().isBlank() ? "Periodo actual" : v.getPeriods())
                + "\n\nObservaciones: " + (v.getNotes() == null || v.getNotes().isBlank() ? "Sin observaciones registradas" : v.getNotes()));
    }

    private void downloadRequest(AdministrativeRequest v) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Descargar soporte");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt"));
        chooser.setInitialFileName(filingNumber(v) + "_soporte.txt");
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("TALENTO 360 HUMANO\n");
            writer.write("Gobernación de Boyacá\n\n");
            writer.write("Radicado: " + filingNumber(v) + "\n");
            writer.write("Tipo: " + recordType(v) + "\n");
            writer.write("Servidor: " + v.getPerson() + "\n");
            writer.write("Documento: " + v.getDocument() + "\n");
            writer.write("Dependencia: " + v.getDepartment() + "\n");
            writer.write("Cargo: " + v.getJobTitle() + "\n");
            writer.write("Fecha inicio: " + v.getStartDate() + "\n");
            writer.write("Días: " + v.getTotalDays() + "\n");
            writer.write("Estado: " + recordStatus(v) + "\n");
            showAlert("Descarga lista", "El soporte fue generado correctamente.");
        } catch (Exception e) {
            showAlert("Error", "No se pudo descargar el soporte: " + e.getMessage());
        }
    }

    private HBox footerBar() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("footer-bar");
        Label fl = new Label("Talento 360 Humano   •   Gobernación de Boyacá   •   2026");
        fl.getStyleClass().add("muted");
        footer.getChildren().add(fl);
        return footer;
    }

    private String format(int n) {
        return String.format("%,d", n).replace(',', '.');
    }

    private String filingNumber(AdministrativeRequest v) {
        String type = recordType(v);
        String prefix = switch (type) {
            case "Incapacidad" -> "INCA";
            case "Permiso" -> "PER";
            case "Licencia maternidad" -> "MAT";
            default -> "VAC";
        };
        int id = v.getRequestId() == 0 ? 999 : v.getRequestId();
        return prefix + "-2026-" + String.format("%05d", Math.abs(id));
    }

    private String recordType(AdministrativeRequest v) {
        String noteText = v.getNotes() == null ? "" : v.getNotes().toLowerCase();
        String employmentText = v.getRequestType() == null ? "" : v.getRequestType().toLowerCase();
        if (employmentText.contains("matern")) return "Licencia maternidad";
        if (employmentText.contains("permiso")) return "Permiso";
        if (employmentText.contains("incap")) return "Incapacidad";
        if (employmentText.contains("vacacion") || employmentText.contains("vacaciones")) return "Vacaciones";
        if (noteText.contains("matern")) return "Licencia maternidad";
        if (noteText.contains("permiso")) return "Permiso";
        if (noteText.contains("incap")) return "Incapacidad";
        if (noteText.contains("vacacion") || noteText.contains("vacaciones")) return "Vacaciones";
        int mod = Math.abs(v.getRequestId()) % 6;
        return switch (mod) {
            case 0 -> "Incapacidad";
            case 1 -> "Permiso";
            case 2 -> "Licencia maternidad";
            default -> "Vacaciones";
        };
    }

    private String recordStatus(AdministrativeRequest v) {
        if (v != null && statusOverrides.containsKey(v.getRequestId())) return normalizeRequestStatus(statusOverrides.get(v.getRequestId()));
        if (v != null && v.getStatus() != null && !v.getStatus().isBlank()) return normalizeRequestStatus(v.getStatus());
        int mod = Math.abs(v.getRequestId()) % 5;
        return switch (mod) {
            case 0 -> "Aprobada";
            case 1 -> "Finalizada";
            case 2 -> "Rechazada";
            case 3 -> "En revisión";
            default -> "Pendiente";
        };
    }

    private String normalizeRequestStatus(String status) {
        if (status == null || status.isBlank()) return "Pendiente";
        String value = status.trim().toLowerCase();
        if (value.contains("aprob")) return "Aprobada";
        if (value.contains("final")) return "Finalizada";
        if (value.contains("rechaz")) return "Rechazada";
        if (value.contains("revisi")) return "En revisión";
        return "Pendiente";
    }

    private String endDate(AdministrativeRequest request) {
        if (request == null) return "Pendiente";
        LocalDate startDate = parseUiDate(request.getStartDate());
        int days = parsePositiveDays(request.getTotalDays());
        if (startDate == null || days <= 0) return endDate(request.getStartDate());
        return formatUiDate(startDate.plusDays(days - 1L));
    }

    private String endDate(String date) {
        return date == null || date.isBlank() ? "Pendiente" : date;
    }

    private String requestDate(AdministrativeRequest v) {
        if (v != null && v.getRequestDate() != null && !v.getRequestDate().isBlank()) {
            LocalDate requestDate = parseUiDate(v.getRequestDate());
            return requestDate == null ? v.getRequestDate() : formatUiDate(requestDate);
        }
        int day = 1 + Math.abs(v.getRequestId()) % 28;
        int month = 1 + Math.abs(v.getRequestId()) % 6;
        return String.format("%02d/%02d/2025", day, month);
    }

    private String formatUiDate(LocalDate date) {
        return date == null ? "" : date.format(UI_DATE);
    }

    private LocalDate parseUiDate(String value) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim();
        for (DateTimeFormatter formatter : List.of(
                UI_DATE,
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        )) {
            try {
                return LocalDate.parse(clean, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private ImageView image(String path, double width, double height, boolean preserve) {
        ImageView view = new ImageView();
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                Image img = new Image(stream);
                view.setImage(img);
            }
        } catch (Exception ignored) {}
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(preserve);
        return view;
    }

    private String value(Employee s, java.util.function.Function<Employee, String> getter) {
        if (s == null) return "No registrado";
        String v = getter.apply(s);
        return v == null || v.isBlank() ? "No registrado" : v;
    }

    private void showLogoutConfirmation() {
        if (appFrame == null) {
            showLogin();
            return;
        }
        VBox card = new VBox(16);
        card.getStyleClass().add("modal-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_logout_unique.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box-danger");
        VBox texts = new VBox(3);
        Label title = new Label("¿Cerrar sesión?");
        title.getStyleClass().add("modal-title");
        Label subtitle = new Label("Tu sesión actual se cerrará y volverás a la pantalla de inicio.");
        subtitle.getStyleClass().add("modal-subtitle");
        texts.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconBox, texts);

        Label detail = new Label("Antes de salir, verifica que no tengas formularios pendientes o archivos sin exportar.");
        detail.getStyleClass().add("modal-message");
        detail.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button logout = new Button("Sí, cerrar sesión");
        logout.getStyleClass().add("danger-button");
        actions.getChildren().addAll(cancel, logout);

        card.getChildren().addAll(header, detail, actions);
        StackPane overlay = modalOverlay(card);
        cancel.setOnAction(e -> closeOverlay(overlay));
        logout.setOnAction(e -> {
            closeOverlay(overlay);
            appFrame = null;
            showLogin();
        });
    }

    private void showAlert(String title, String message) {
        if (appFrame == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            return;
        }

        VBox card = new VBox(16);
        card.getStyleClass().add("modal-card");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane(image("/assets/icon_incapacity_unique_two.png", 28, 28, true));
        iconBox.getStyleClass().add("modal-icon-box");
        VBox texts = new VBox(3);
        Label t = new Label(title);
        t.getStyleClass().add("modal-title");
        Label st = new Label("Información del sistema Talento 360 Humano");
        st.getStyleClass().add("modal-subtitle");
        texts.getChildren().addAll(t, st);
        header.getChildren().addAll(iconBox, texts);

        Label msg = new Label(message);
        msg.getStyleClass().add("modal-message");
        msg.setWrapText(true);
        msg.setMaxWidth(520);

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button ok = new Button("Entendido");
        ok.getStyleClass().add("green-button");
        actions.getChildren().add(ok);

        card.getChildren().addAll(header, msg, actions);
        StackPane overlay = modalOverlay(card);
        ok.setOnAction(e -> closeOverlay(overlay));
    }

    private StackPane modalOverlay(VBox card) {
        boolean large = card.getStyleClass().contains("modal-card-large");
        card.setPrefWidth(large ? 760 : 520);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setMinHeight(Region.USE_PREF_SIZE);

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("modal-overlay");
        overlay.setAlignment(Pos.CENTER);
        appFrame.getChildren().add(overlay);
        return overlay;
    }

    private void closeOverlay(StackPane overlay) {
        if (appFrame != null && overlay != null) {
            appFrame.getChildren().remove(overlay);
        }
    }

    private List<Map<String, String>> pageSlice(List<Map<String, String>> rows, int page, int pageSize) {
        if (rows == null || rows.isEmpty()) return List.of();
        int safePage = Math.max(1, page);
        int from = Math.min((safePage - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());
        return new ArrayList<>(rows.subList(from, to));
    }

    private void installFullWindowGuard() {
        stage.iconifiedProperty().addListener((noteText, wasIconified, isIconified) -> {
            if (!isIconified) Platform.runLater(this::forceMaximized);
        });
        stage.maximizedProperty().addListener((noteText, wasMaximized, isMaximized) -> {
            if (!isMaximized && !stage.isIconified()) {
                Platform.runLater(this::forceMaximized);
            }
        });
        stage.sceneProperty().addListener((noteText, oldScene, newScene) -> Platform.runLater(this::forceMaximized));
    }

    private void forceMaximized() {
        if (stage == null) return;
        stage.setFullScreen(false);
        applyVisualBounds();
        stage.setMaximized(true);
        javafx.application.Platform.runLater(() -> {
            applyVisualBounds();
            stage.setMaximized(true);
        });
        javafx.application.Platform.runLater(() -> javafx.application.Platform.runLater(() -> {
            applyVisualBounds();
            stage.setMaximized(true);
        }));
    }

    private void applyVisualBounds() {
        try {
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        } catch (Exception ignored) {}
    }

    private String upperUi(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toUpperCase(new java.util.Locale("es", "CO"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
