package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Candidate;
import models.Certificate;
import services.CandidateService;
import services.CertificateService;
import services.I18nService;
import services.SceneService;
import services.SessionService;

import java.time.LocalDate;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CertificateController {
    private static final String INTEGER_INPUT_PATTERN = "\\d*";
    private final CertificateService certificateService = new CertificateService();
    private final CandidateService candidateService = new CandidateService();
    private final ObservableList<Certificate> certificates = FXCollections.observableArrayList();
    private final ObservableList<Candidate> readyCandidates = FXCollections.observableArrayList();

    @FXML private VBox certificateFormPane;
    @FXML private TextField searchField;
    @FXML private TextField candidateIdField;
    @FXML private TextField certificateNumberField;
    @FXML private DatePicker issuedDatePicker;
    @FXML private TextArea notesArea;
    @FXML private Label certifiedCountLabel;
    @FXML private Label readyCountLabel;
    @FXML private MenuButton languageMenuButton;
    @FXML private TableView<Certificate> certificatesTable;
    @FXML private TableColumn<Certificate, Integer> idColumn;
    @FXML private TableColumn<Certificate, Integer> candidateIdColumn;
    @FXML private TableColumn<Certificate, String> certificateNumberColumn;
    @FXML private TableColumn<Certificate, LocalDate> issuedDateColumn;
    @FXML private TableView<Candidate> readyCandidatesTable;
    @FXML private TableColumn<Candidate, Integer> readyIdColumn;
    @FXML private TableColumn<Candidate, String> readyNameColumn;
    @FXML private TableColumn<Candidate, String> readyEmailColumn;
    @FXML private TableColumn<Candidate, String> readyTrainingColumn;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateIdColumn.setCellValueFactory(new PropertyValueFactory<>("candidateId"));
        certificateNumberColumn.setCellValueFactory(new PropertyValueFactory<>("certificateNumber"));
        issuedDateColumn.setCellValueFactory(new PropertyValueFactory<>("issuedDate"));
        readyIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        readyNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        readyEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        readyTrainingColumn.setCellValueFactory(new PropertyValueFactory<>("trainingName"));
        readyCandidatesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, candidate) -> fillCertificateForm(candidate));
        certificatesTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Certificate certificate = certificatesTable.getSelectionModel().getSelectedItem();
                if (certificate != null) {
                    showCertificatePreview(certificate);
                }
            }
        });
        issuedDatePicker.setValue(LocalDate.now());
        installIntegerFormatter(candidateIdField);
        setSidebarVisible(certificateFormPane, false);
        applyLanguageButtonStyles();
        loadCertificates();
    }

    @FXML
    private void handleGenerateCertificate() {
        try {
            int candidateId = Integer.parseInt(candidateIdField.getText());
            certificateService.save(candidateId, certificateNumberField.getText(), issuedDatePicker.getValue(), notesArea.getText());
            candidateIdField.clear();
            certificateNumberField.clear();
            notesArea.clear();
            issuedDatePicker.setValue(LocalDate.now());
            loadCertificates();
            showCertificatesTable();
        } catch (NumberFormatException e) {
            showWarning(I18nService.get("validation.number"));
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleToggleCertificateForm() {
        setSidebarVisible(certificateFormPane, !certificateFormPane.isVisible());
    }

    @FXML
    private void handleOpenDashboard() {
        SceneService.loadView("main-view.fxml", I18nService.get("app.title"));
    }

    @FXML
    private void handleShowHelp() {
        SceneService.loadView("help-view.fxml", I18nService.get("help.title"));
    }

    @FXML
    private void handleOpenTrainings() {
        SceneService.loadView("trainings-view.fxml", I18nService.get("training.title"));
    }

    @FXML
    private void handleOpenPayments() {
        SceneService.loadView("payments-view.fxml", I18nService.get("payment.title"));
    }

    @FXML
    private void handleOpenCertificates() {
        loadCertificates();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            certificatesTable.setItems(certificates);
            readyCandidatesTable.setItems(readyCandidates);
            return;
        }

        certificatesTable.setItems(certificates.filtered(certificate ->
                String.valueOf(certificate.getId()).contains(query)
                        || String.valueOf(certificate.getCandidateId()).contains(query)
                        || certificate.getCertificateNumber().toLowerCase().contains(query)
                        || String.valueOf(certificate.getIssuedDate()).contains(query)
        ));
        readyCandidatesTable.setItems(readyCandidates.filtered(candidate ->
                String.valueOf(candidate.getId()).contains(query)
                        || candidate.getFullName().toLowerCase().contains(query)
                        || candidate.getEmail().toLowerCase().contains(query)
                        || candidate.getTrainingName().toLowerCase().contains(query)
        ));
    }

    @FXML
    private void handleLogout() {
        if (!confirmAction(I18nService.get("confirm.logout.title"), I18nService.get("confirm.logout"))) {
            return;
        }
        SessionService.clear();
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    private void loadCertificates() {
        certificates.setAll(certificateService.getAll());
        readyCandidates.setAll(certificateService.getReadyForCertification());
        certificatesTable.setItems(certificates);
        readyCandidatesTable.setItems(readyCandidates);
        certifiedCountLabel.setText(String.valueOf(certificates.size()));
        readyCountLabel.setText(String.valueOf(readyCandidates.size()));
    }

    @FXML
    private void handleDownloadCertificatePdf() {
        Certificate certificate = certificatesTable.getSelectionModel().getSelectedItem();
        if (certificate == null) {
            showWarning(I18nService.get("certificate.pdf.select"));
            return;
        }

        Candidate candidate = findCandidateForCertificate(certificate);
        if (candidate == null) {
            showWarning(I18nService.get("validation.candidate.missing"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nService.get("certificate.downloadPdf"));
        fileChooser.setInitialFileName(buildCertificatePdfFileName(certificate, candidate));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(certificatesTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Files.write(file.toPath(), buildCertificatePdf(certificate, candidate));
            showInfo(I18nService.get("certificate.pdf.saved"));
        } catch (Exception e) {
            showWarning(I18nService.get("certificate.pdf.error") + "\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAlbanianLanguage() {
        I18nService.setLanguage("sq");
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleEnglishLanguage() {
        I18nService.setLanguage("en");
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleGermanLanguage() {
        I18nService.setLanguage("de");
        SceneService.loadView("certificates-view.fxml", I18nService.get("certificate.title"));
    }

    @FXML
    private void handleShowReadyCandidates() {
        loadCertificates();
        certificatesTable.setVisible(false);
        certificatesTable.setManaged(false);
        readyCandidatesTable.setVisible(true);
        readyCandidatesTable.setManaged(true);
    }

    @FXML
    private void handleShowCertificates() {
        showCertificatesTable();
    }

    private void showCertificatesTable() {
        clearCertificateForm();
        certificatesTable.setVisible(true);
        certificatesTable.setManaged(true);
        readyCandidatesTable.setVisible(false);
        readyCandidatesTable.setManaged(false);
    }

    private void fillCertificateForm(Candidate candidate) {
        if (candidate == null) {
            return;
        }
        setSidebarVisible(certificateFormPane, true);
        candidateIdField.setText(String.valueOf(candidate.getId()));
        issuedDatePicker.setValue(LocalDate.now());
    }

    private void clearCertificateForm() {
        candidateIdField.clear();
        certificateNumberField.clear();
        notesArea.clear();
        issuedDatePicker.setValue(LocalDate.now());
        readyCandidatesTable.getSelectionModel().clearSelection();
    }

    private void showCertificatePreview(Certificate certificate) {
        Candidate candidate = findCandidateForCertificate(certificate);
        if (candidate == null) {
            showWarning(I18nService.get("validation.candidate.missing"));
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(I18nService.get("certificate.preview.title"));

        Pane certificatePane = buildCertificatePreviewPane(certificate, candidate);

        Scene scene = new Scene(certificatePane, 1000, 710);
        scene.getStylesheets().add(SceneService.class.getResource("/styles/app.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(I18nService.get("dialog.warning"));
        alert.setHeaderText(I18nService.get("dialog.checkData"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18nService.get("certificate.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18nService.get("dialog.confirmation"));
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void setSidebarVisible(VBox sidebar, boolean visible) {
        sidebar.setVisible(visible);
        sidebar.setManaged(visible);
    }

    private void installIntegerFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(INTEGER_INPUT_PATTERN) ? change : null
        ));
    }

    private Candidate findCandidateForCertificate(Certificate certificate) {
        return candidateService.getAll().stream()
                .filter(item -> item.getId() == certificate.getCandidateId())
                .findFirst()
                .orElse(null);
    }

    private Pane buildCertificatePreviewPane(Certificate certificate, Candidate candidate) {
        Pane pane = new Pane();
        pane.setPrefSize(1000, 710);
        pane.setStyle("-fx-background-color: #ffffff;");

        double scale = 1000.0 / 842.0;
        pane.getChildren().addAll(
                rect(0, 0, 1000, 710, "#ffffff", null, 0),
                rect(45 * scale, 45 * scale, 752 * scale, 505 * scale, null, "#dddddd", 14),
                rect(63 * scale, 63 * scale, 716 * scale, 469 * scale, null, "#f0f0f0", 10),
                rect(75 * scale, 75 * scale, 692 * scale, 445 * scale, null, "#0a3a52", 4),
                polygon("#0a3a52", 0, 710, 1000, 710, 1000, 660, 665, 660, 617, 625, 0, 625),
                polygon("#155f85", 0, 505, 0, 442, 66, 400, 66, 463),
                polygon("#0a3a52", 918, 83, 1000, 0, 1000, 710, 882, 710, 940, 532),
                polygon("#155f85", 548, 0, 1000, 0, 1000, 52, 589, 52),
                polygon("#ffca40", 784, 0, 882, 0, 1000, 565, 1000, 625),
                polygon("#ffad32", 803, 0, 837, 0, 1000, 515, 1000, 565),
                polygon("#ffca40", 0, 232, 0, 174, 630, 710, 718, 710),
                polygon("#ffad32", 0, 205, 0, 166, 665, 710, 708, 710),
                polygon("#f4f4f4", 86, 625, 475, 625, 86, 346),
                polygon("#fafafa", 890, 83, 706, 83, 890, 390)
        );

        addCenteredText(pane, "CERTIFICATE", 500, 160, 50, Font.font("Times New Roman", FontWeight.BOLD, 50), 9, "#292929");
        addCenteredText(pane, "OF ACHIEVEMENT", 500, 215, 28, Font.font("Times New Roman", FontWeight.NORMAL, 28), 5, "#292929");
        addDiamondShape(pane, 474, 250, 9, "#ffad32");
        addDiamondShape(pane, 500, 250, 12, "#155f85");
        addDiamondShape(pane, 526, 250, 9, "#ffad32");
        addCenteredText(pane, "This certificate is proudly presented to", 500, 300, 16, Font.font("Times New Roman", FontWeight.NORMAL, 16), 0, "#292929");
        addCenteredText(pane, candidate.getFullName(), 500, 365, 48, Font.font("Times New Roman", FontPosture.ITALIC, 48), 0, "#292929");
        addCenteredText(pane, "for successfully completing the training program", 500, 410, 15, Font.font("Times New Roman", FontWeight.NORMAL, 15), 0, "#292929");
        addCenteredText(pane, candidate.getTrainingName(), 500, 440, 23, Font.font("Times New Roman", FontWeight.BOLD, 23), 1, "#292929");
        addCenteredText(pane, getCertificateNotes(certificate), 500, 485, 14, Font.font("Times New Roman", FontWeight.NORMAL, 14), 0, "#292929");
        addCenteredText(pane, I18nService.get("certificate.number") + ": " + certificate.getCertificateNumber(), 500, 515, 13, Font.font("Helvetica", 13), 0, "#292929");

        addSealShape(pane);
        addSignatureShape(pane, 140, 592, "Training Center");
        addSignatureShape(pane, 700, 592, I18nService.get("certificate.date") + ": " + certificate.getIssuedDate());
        return pane;
    }

    private String getCertificateNotes(Certificate certificate) {
        return certificate.getNotes().isBlank()
                ? I18nService.get("certificate.preview.footer")
                : certificate.getNotes();
    }

    private Rectangle rect(double x, double y, double width, double height, String fill, String stroke, double strokeWidth) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(fill == null ? Color.TRANSPARENT : Color.web(fill));
        rectangle.setStroke(stroke == null ? Color.TRANSPARENT : Color.web(stroke));
        rectangle.setStrokeWidth(strokeWidth);
        return rectangle;
    }

    private Polygon polygon(String fill, double... points) {
        Polygon polygon = new Polygon(points);
        polygon.setFill(Color.web(fill));
        return polygon;
    }

    private void addCenteredText(Pane pane, String value, double centerX, double y, double wrappingWidth, Font font, double letterSpacing, String color) {
        Text text = new Text(value == null ? "" : value);
        text.setFont(font);
        text.setFill(Color.web(color));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(wrappingWidth > 60 ? wrappingWidth : 760);
        text.setX(centerX - text.getWrappingWidth() / 2);
        text.setY(y);
        if (letterSpacing > 0) {
            text.setStyle("-fx-letter-spacing: " + letterSpacing + "px;");
        }
        pane.getChildren().add(text);
    }

    private void addDiamondShape(Pane pane, double x, double y, double size, String color) {
        pane.getChildren().add(polygon(color, x, y - size, x + size, y, x, y + size, x - size, y));
    }

    private void addSealShape(Pane pane) {
        Circle outer = new Circle(500, 575, 47, Color.web("#0a3a52"));
        Circle middle = new Circle(500, 575, 36, Color.web("#ffca40"));
        Circle inner = new Circle(500, 575, 29, Color.web("#155f85"));
        pane.getChildren().addAll(
                polygon("#ffca40", 470, 608, 483, 608, 475, 675, 456, 657),
                polygon("#ffca40", 517, 608, 530, 608, 544, 657, 525, 675),
                outer, middle, inner
        );
        addCenteredText(pane, "IT", 500, 566, 80, Font.font("Times New Roman", FontWeight.BOLD, 16), 2, "#0a3a52");
        addCenteredText(pane, "CENTER", 500, 592, 100, Font.font("Times New Roman", FontWeight.BOLD, 10), 3, "#0a3a52");
    }

    private void addSignatureShape(Pane pane, double x, double y, String label) {
        Line line = new Line(x, y, x + 210, y);
        line.setStroke(Color.web("#155f85"));
        line.setStrokeWidth(3);
        pane.getChildren().add(line);
        addCenteredText(pane, label, x + 105, y + 31, 260, Font.font("Times New Roman", 14), 0, "#292929");
    }

    private byte[] buildCertificatePdf(Certificate certificate, Candidate candidate) {
        StringBuilder content = new StringBuilder();

        appendCertificateBackground(content);
        appendCenteredText(content, "CERTIFICATE", 421, 468, 42, "F2", 9);
        appendCenteredText(content, "OF ACHIEVEMENT", 421, 432, 23, "F1", 4);

        appendDiamond(content, 400, 404, 7, "1 0.68 0.18");
        appendDiamond(content, 421, 404, 9, "0.08 0.36 0.52");
        appendDiamond(content, 442, 404, 7, "1 0.68 0.18");

        appendCenteredText(content, "This certificate is proudly presented to", 421, 366, 12, "F1", 0);
        appendCenteredText(content, candidate.getFullName(), 421, 312, 38, "F3", 0);
        appendCenteredText(content, "for successfully completing the training program", 421, 277, 12, "F1", 0);
        appendCenteredText(content, candidate.getTrainingName(), 421, 253, 18, "F2", 0);

        appendCenteredText(content, limitPdfText(getCertificateNotes(certificate), 88), 421, 225, 11, "F1", 0);
        appendCenteredText(content, I18nService.get("certificate.number") + ": " + certificate.getCertificateNumber(), 421, 202, 10, "F4", 0);

        appendSeal(content);
        appendSignature(content, 120, 126, "Training Center");
        appendSignature(content, 560, 126, I18nService.get("certificate.date") + ": " + certificate.getIssuedDate());

        return createSimplePdf(content.toString());
    }

    private byte[] createSimplePdf(String content) {
        byte[] streamBytes = content.getBytes(StandardCharsets.ISO_8859_1);
        String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources << /Font << /F1 4 0 R /F2 5 0 R /F3 6 0 R /F4 7 0 R >> >> /Contents 8 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Times-Roman >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Times-Bold >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Times-Italic >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + streamBytes.length + " >>\nstream\n" + content + "endstream"
        };

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length + 1];
        for (int i = 0; i < objects.length; i++) {
            offsets[i + 1] = pdf.length();
            pdf.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 ").append(objects.length + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i <= objects.length; i++) {
            pdf.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private void appendCertificateBackground(StringBuilder content) {
        content.append("1 1 1 rg 0 0 842 595 re f\n");
        content.append("0.88 0.88 0.88 RG 14 w 38 38 766 519 re S\n");
        content.append("0.95 0.95 0.95 RG 10 w 58 58 726 479 re S\n");
        content.append("0.04 0.22 0.32 RG 5 w 70 70 702 455 re S\n");

        content.append("0.04 0.22 0.32 rg 0 0 m 842 0 l 842 44 l 560 44 l 520 70 l 0 70 l f\n");
        content.append("0.08 0.36 0.52 rg 0 448 m 0 388 l 55 350 l 55 410 l f\n");
        content.append("0.04 0.22 0.32 rg 772 70 m 842 0 l 842 595 l 742 595 l 792 446 l f\n");
        content.append("0.08 0.36 0.52 rg 462 595 m 842 595 l 842 552 l 496 552 l f\n");

        content.append("1 0.78 0.25 rg 660 595 m 742 595 l 842 124 l 842 72 l f\n");
        content.append("1 0.62 0.2 rg 676 595 m 704 595 l 842 166 l 842 126 l f\n");
        content.append("1 0.78 0.25 rg 0 195 m 0 145 l 530 0 l 604 0 l f\n");
        content.append("1 0.62 0.2 rg 0 172 m 0 140 l 560 0 l 596 0 l f\n");

        content.append("0.96 0.96 0.96 rg 72 72 m 400 72 l 72 292 l f\n");
        content.append("0.98 0.98 0.98 rg 750 525 m 595 525 l 750 222 l f\n");
    }

    private void appendCenteredText(StringBuilder content, String text, int centerX, int y, int size, String font, int letterSpacing) {
        int estimatedWidth = estimateTextWidth(text, size, letterSpacing);
        int x = centerX - estimatedWidth / 2;
        content.append("0.16 0.16 0.16 rg\n");
        content.append("BT /").append(font).append(" ").append(size).append(" Tf ");
        content.append(letterSpacing).append(" Tc ");
        content.append(x).append(" ").append(y).append(" Td (").append(escapePdf(text)).append(") Tj ET\n");
    }

    private int estimateTextWidth(String text, int size, int letterSpacing) {
        int length = text == null ? 0 : text.length();
        return (int) (length * size * 0.48 + Math.max(0, length - 1) * letterSpacing);
    }

    private String limitPdfText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3).trim() + "...";
    }

    private void appendDiamond(StringBuilder content, int x, int y, int size, String color) {
        content.append(color).append(" rg ");
        content.append(x).append(" ").append(y + size).append(" m ");
        content.append(x + size).append(" ").append(y).append(" l ");
        content.append(x).append(" ").append(y - size).append(" l ");
        content.append(x - size).append(" ").append(y).append(" l f\n");
    }

    private void appendSeal(StringBuilder content) {
        appendCircle(content, 421, 159, 38, "0.04 0.22 0.32");
        appendCircle(content, 421, 159, 29, "1 0.78 0.25");
        appendCircle(content, 421, 159, 24, "0.08 0.36 0.52");
        content.append("1 0.78 0.25 rg 397 127 m 407 127 l 400 78 l 385 92 l f\n");
        content.append("1 0.78 0.25 rg 435 127 m 445 127 l 457 92 l 442 78 l f\n");
        appendCenteredText(content, "IT", 421, 166, 13, "F2", 0);
        appendCenteredText(content, "CENTER", 421, 150, 9, "F2", 0);
    }

    private void appendCircle(StringBuilder content, int centerX, int centerY, int radius, String color) {
        double c = radius * 0.5522847498;
        content.append(color).append(" rg ");
        content.append(centerX).append(" ").append(centerY + radius).append(" m ");
        content.append(format(centerX + c)).append(" ").append(centerY + radius).append(" ")
                .append(centerX + radius).append(" ").append(format(centerY + c)).append(" ")
                .append(centerX + radius).append(" ").append(centerY).append(" c ");
        content.append(centerX + radius).append(" ").append(format(centerY - c)).append(" ")
                .append(format(centerX + c)).append(" ").append(centerY - radius).append(" ")
                .append(centerX).append(" ").append(centerY - radius).append(" c ");
        content.append(format(centerX - c)).append(" ").append(centerY - radius).append(" ")
                .append(centerX - radius).append(" ").append(format(centerY - c)).append(" ")
                .append(centerX - radius).append(" ").append(centerY).append(" c ");
        content.append(centerX - radius).append(" ").append(format(centerY + c)).append(" ")
                .append(format(centerX - c)).append(" ").append(centerY + radius).append(" ")
                .append(centerX).append(" ").append(centerY + radius).append(" c f\n");
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void appendSignature(StringBuilder content, int x, int y, String label) {
        content.append("0.08 0.36 0.52 RG 2 w ").append(x).append(" ").append(y).append(" m ").append(x + 170).append(" ").append(y).append(" l S\n");
        appendCenteredText(content, label, x + 85, y - 24, 11, "F1", 0);
    }

    private String escapePdf(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("ë", "e")
                .replace("Ë", "E")
                .replace("ç", "c")
                .replace("Ç", "C");
    }

    private String buildCertificatePdfFileName(Certificate certificate, Candidate candidate) {
        String training = sanitizeFileName(candidate.getTrainingName());
        String certificateNumber = sanitizeFileName(certificate.getCertificateNumber());
        String uniqueNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return training + "_certifikate_" + certificateNumber + "_" + uniqueNumber + ".pdf";
    }

    private String sanitizeFileName(String value) {
        String cleaned = value == null ? "certifikate" : value.trim()
                .replace("ë", "e")
                .replace("Ë", "E")
                .replace("ç", "c")
                .replace("Ç", "C")
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return cleaned.isBlank() ? "certifikate" : cleaned;
    }

    private void applyLanguageButtonStyles() {
        languageMenuButton.setText(I18nService.getCurrentLanguageLabel());
    }
}