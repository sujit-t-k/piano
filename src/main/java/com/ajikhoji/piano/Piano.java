package com.ajikhoji.piano;

import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author Sujit T K
 */
public class Piano extends Application {

    private PianoKeyNotes pkn = new PianoKeyNotes();
    private PianoKey[] pk = new PianoKey[12];
    private Recorder r = new Recorder();
    private Text txtStatus = new Text("Ready");
    private Pane sb = getStatusBar();
    private Timeline tl_blink = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(txtStatus.opacityProperty(),0.0D)),
            new KeyFrame(Duration.millis(1400.0D), new KeyValue(txtStatus.opacityProperty(),1.0D)),
            new KeyFrame(Duration.millis(2100.0D), new KeyValue(txtStatus.opacityProperty(),0.0D)));
    private Pane paneShowNotesUI = null;
    private final double DBL_KEY_WIDTH = 70.0D;
    @Override
    public void start(Stage primaryStage) {
        Pane piano_keys = new Pane();//#040720, #828C64
        piano_keys.setBackground(new Background(new BackgroundFill(Color.web("#040720"), CornerRadii.EMPTY, Insets.EMPTY)));
        paneShowNotesUI = new Pane();
        paneShowNotesUI.setMinSize(836.0D, 600.0D);
        paneShowNotesUI.setMaxSize(836.0D, 600.0D);
        paneShowNotesUI.setPrefSize(836.0D, 600.0D);
        paneShowNotesUI.setLayoutX(92.0D);
        paneShowNotesUI.setLayoutY(30.0D);
        paneShowNotesUI.setBackground(new Background(new BackgroundFill(Color.web("#040720"), CornerRadii.EMPTY, Insets.EMPTY)));
        piano_keys.getChildren().add(paneShowNotesUI);
        Scene scene = new Scene(piano_keys, 1200, 450);
        double x_loc = 90.0D;
        Stop[] stops = new Stop[]{new Stop(0, Color.WHEAT), new Stop(1, Color.WHITE)};
        Stop[] stopsHover = new Stop[]{new Stop(0, Color.WHEAT), new Stop(1, Color.web("#C3D969"))};//DARKGOLDENROD
        Stop[] stopsPressed1 = new Stop[]{new Stop(0, Color.WHEAT), new Stop(1, Color.INDIGO)};
        Stop[] stopsPressed2 = new Stop[]{new Stop(0, Color.WHEAT), new Stop(1, Color.web("#8C0C44"))};
        Stop[] stopsPressed3 = new Stop[]{new Stop(0, Color.WHEAT), new Stop(1, Color.web("#0C8C57"))};
        LinearGradient lgNormal = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, stops);
        LinearGradient lgHover = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, stopsHover);
        for (int i = 0; i < 12; i++) {
            final String CLR_PRESSED = (i % 3 == 0) ? "#69E81A" : (i % 3 == 1) ? "#31C4CC" : "#FC0384";
            LinearGradient lgPressed = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, (i % 3 == 0) ? stopsPressed2 : (i % 3 == 2) ? stopsPressed1 : stopsPressed3);
            pk[i] = new PianoKey(DBL_KEY_WIDTH, 220.0D, 4.0D, lgNormal, lgHover, lgPressed);
            pk[i].rect.setLayoutX(x_loc);
            pk[i].rect.setLayoutY(440.0D);
            final int locnum = i;
            pk[i].rect.setOnMousePressed(e -> {
                playNote(pkn.sfx_key[locnum]);
                pk[locnum].showKeyPressedEffect();
                if (r.onrecord) {
                    r.arrStrKeyID.add(locnum); r.arrStrTimeStamp.add(r.dblRecordMS);
                    r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), eh -> {
                        playNote(pkn.sfx_key[locnum]);
                        pk[locnum].showKeyPressedEffect();
                    }));
                }
            });
            piano_keys.getChildren().add(pk[i].rect);
            x_loc += 70.0D;
        }
        x_loc = 90.0D;
        for (int i = 0; i < 11; i++) {
            x_loc += 70.0D;
            if (i == 5) {
                continue;
            }
            final String CLR_PRESSED = (i % 3 == 2) ? "69E81A" : (i % 3 == 0) ? "#31C4CC" : "#FC0384";
            PianoKey p = new PianoKey(40.0D, 145.0D, 2.0D, Color.web("#010203"), Color.web("#C25A71"), Color.web(CLR_PRESSED));
            p.rect.setLayoutX(x_loc - 20.0D);
            p.rect.setLayoutY(440.0D);
            piano_keys.getChildren().add(p.rect);
        }
        Line l = new Line(90.0D, 440.0D, pk[11].rect.getLayoutX() + 70.0D, 440.0D);
        l.setStroke(Color.web("#552EED"));
        Glow g = new Glow();
        g.setLevel(0.0D);
        Bloom b = new Bloom();
        b.setInput(g);
        b.setThreshold(0.0D);
        l.setEffect(b);
        Timeline t = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(g.levelProperty(), 0.0D)),
                new KeyFrame(Duration.millis(400.0D), new KeyValue(g.levelProperty(), 1.0D)),
                new KeyFrame(Duration.millis(1200.0D), new KeyValue(g.levelProperty(), -0.7D)),
                new KeyFrame(Duration.ZERO, new KeyValue(b.thresholdProperty(), 0.9D)),
                new KeyFrame(Duration.millis(400.0D), new KeyValue(b.thresholdProperty(), -0.6D)),
                new KeyFrame(Duration.millis(1200.0D), new KeyValue(b.thresholdProperty(), 0.2D)));
        t.setCycleCount(Timeline.INDEFINITE);
        t.setAutoReverse(true);
        t.play();
        l.setStrokeWidth(6.0D);
        piano_keys.getChildren().add(l);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(-5.0D);
        ds.setRadius(50.0D);
        ds.setInput(b);
        ds.setColor(Color.web("#ED16AC"));
        BoxBlur bb = new BoxBlur();
        Timeline td = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(ds.radiusProperty(), 5.0D)),
                new KeyFrame(Duration.millis(2000.0D), new KeyValue(ds.radiusProperty(), 30.0D)),
                new KeyFrame(Duration.millis(4500.0D), new KeyValue(ds.radiusProperty(), 70.0D)),
                new KeyFrame(Duration.millis(6900.0D), new KeyValue(ds.radiusProperty(), 150.0D)));
        td.setCycleCount(Timeline.INDEFINITE);
        td.setAutoReverse(true);
        bb.setInput(ds);
        td.play();
        l.setEffect(bb);
        scene.setOnKeyPressed((KeyEvent event) -> {
            switch (event.getCode()) {
                case Q:
                    playNote(pkn.sfx_key[0]);
                    pk[0].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(0); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[0]);
                            pk[0].showKeyPressedEffect();
                        }));
                    }
                    break;
                case A:
                    playNote(pkn.sfx_key[1]);
                    pk[1].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(1); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[1]);
                            pk[1].showKeyPressedEffect();
                        }));
                    }
                    break;
                case Z:
                    playNote(pkn.sfx_key[2]);
                    pk[2].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(2); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[2]);
                            pk[2].showKeyPressedEffect();
                        }));
                    }
                    break;
                case X:
                    playNote(pkn.sfx_key[3]);
                    pk[3].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(3); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[3]);
                            pk[3].showKeyPressedEffect();
                        }));
                    }
                    break;
                case C:
                    playNote(pkn.sfx_key[4]);
                    pk[4].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(4); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[4]);
                            pk[4].showKeyPressedEffect();
                        }));
                    }
                    break;
                case V:
                    playNote(pkn.sfx_key[5]);
                    pk[5].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(5); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[5]);
                            pk[5].showKeyPressedEffect();
                        }));
                    }
                    break;
                case B:
                    playNote(pkn.sfx_key[6]);
                    pk[6].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(6); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[6]);
                            pk[6].showKeyPressedEffect();
                        }));
                    }
                    break;
                case N:
                    playNote(pkn.sfx_key[7]);
                    pk[7].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(7); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[7]);
                            pk[7].showKeyPressedEffect();
                        }));
                    }
                    break;
                case M:
                    playNote(pkn.sfx_key[8]);
                    pk[8].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(8); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[8]);
                            pk[8].showKeyPressedEffect();
                        }));
                    }
                    break;
                case L:
                    playNote(pkn.sfx_key[9]);
                    pk[9].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(9); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[9]);
                            pk[9].showKeyPressedEffect();
                        }));
                    }
                    break;
                case P:
                    playNote(pkn.sfx_key[10]);
                    pk[10].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(10); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[10]);
                            pk[10].showKeyPressedEffect();
                        }));
                    }
                    break;
                case Y:
                    playNote(pkn.sfx_key[11]);
                    pk[11].showKeyPressedEffect();
                    if (r.onrecord) {
                        r.arrStrKeyID.add(11); r.arrStrTimeStamp.add(r.dblRecordMS);
                        r.tl_recorded.getKeyFrames().add(new KeyFrame(Duration.millis(r.dblRecordMS), e -> {
                            playNote(pkn.sfx_key[11]);
                            pk[11].showKeyPressedEffect();
                        }));
                    }
                    break;
            }
        });
        Pane dp = getPlayerPane("#c41495", "#16065c");
        dp.setLayoutX(1020.0D);
        dp.setLayoutY(680.0D);
        Text txtPiano = new Text("Piano");
        txtPiano.setLayoutX(200.0D+800.0D);
        txtPiano.setLayoutY(200.0D);
        txtPiano.setFill(Color.RED);
        txtPiano.setFont(Font.font("Bradley Hand ITC", FontWeight.BOLD, FontPosture.REGULAR, 140));
        piano_keys.getChildren().addAll(dp,sb,getTitleBar(),txtPiano);
        primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private double locX, locY;

    private DraggablePane getPlayerPane(final String strBorderColor, final String strPaneColor) {
        DraggablePane dp = new DraggablePane(500.0D, 120.0D, strBorderColor, strPaneColor);
        Polygon p = new Polygon();
        p.setOnMousePressed((MouseEvent e) -> {
            locX = e.getX();
            locY = e.getY();
        });
        p.setOnMouseDragged((MouseEvent event) -> {
            if (event.getSceneX() > 20.0D && event.getSceneY() > 20.0D) {
                dp.setLayoutX(event.getSceneX() - locX);
                dp.setLayoutY(event.getSceneY() - locY);
            }
        });
        p.getPoints().addAll(0.0D, 0.0D, 300.0D, 0.0D, 270.0D, 25.0D, 0.0D, 25.0D, 0.0D, 0.0D);
        p.setFill(Color.web(strBorderColor));
        Text t_title = new Text("Player");
        t_title.setMouseTransparent(true);
        t_title.setLayoutX(12.0D);
        t_title.setLayoutY(18.0D);
        t_title.setFont(Font.font("Century Gothic", FontWeight.BOLD, FontPosture.REGULAR, 22));
        t_title.setFill(Color.web(strPaneColor));
        Button btnRecord = new Button("RECORD");
        btnRecord.setLayoutX(50.0D);
        btnRecord.setLayoutY(80.0D);
        Button btnStop = new Button("STOP");
        btnStop.setLayoutX(120.0D);
        btnStop.setLayoutY(80.0D);
        Button btnPause = new Button("PAUSE");
        btnPause.setLayoutX(300.0D);
        btnPause.setLayoutY(80.0D);
        Button btnResume = new Button("RESUME");
        btnResume.setLayoutX(370.0D);
        btnResume.setLayoutY(80.0D);
        Button btnPlay = new Button("PLAY");
        btnStop.setDisable(true);
        btnPause.setDisable(true);
        btnPlay.setDisable(true);
        btnResume.setDisable(true);
        btnRecord.setOnAction(e -> {
            btnRecord.setDisable(true);
            btnStop.setDisable(false);
            btnPause.setDisable(false);
            btnPlay.setDisable(true);
            btnResume.setDisable(true);
            r.startRecord();
        });
        btnStop.setOnAction(e -> {
            btnRecord.setDisable(false);
            btnStop.setDisable(true);
            btnPause.setDisable(true);
            btnPlay.setDisable(false);
            btnResume.setDisable(true);
            r.stopRecord();
        });
        btnResume.setOnAction(e -> {
            btnRecord.setDisable(true);
            btnStop.setDisable(false);
            btnPause.setDisable(false);
            btnPlay.setDisable(true);
            btnResume.setDisable(true);
            r.resumeRecord();
        });
        btnPause.setOnAction(e -> {
            r.pauseRecord();
            btnRecord.setDisable(true);
            btnStop.setDisable(false);
            btnPause.setDisable(true);
            btnPlay.setDisable(true);
            btnResume.setDisable(false);
        });
        btnPlay.setOnAction(e -> {
            btnRecord.setDisable(true);
            btnStop.setDisable(true);
            btnPause.setDisable(true);
            btnPlay.setDisable(true);
            btnResume.setDisable(true);
            Timeline t_blocks = new Timeline();
            for(int i = 0; i < r.arrStrKeyID.size(); i++) {
                Rectangle rectTemp = new Rectangle(this.DBL_KEY_WIDTH*(r.arrStrKeyID.get(i)),-this.DBL_KEY_WIDTH,this.DBL_KEY_WIDTH,this.DBL_KEY_WIDTH);
                rectTemp.setFill(Color.web((r.arrStrKeyID.get(i) % 3 == 0) ? "#29DB41" : ((r.arrStrKeyID.get(i) % 3 == 1) ? "#406DDB" : "#DB1B98")));
                t_blocks.getKeyFrames().addAll(new KeyFrame(Duration.millis(r.arrStrTimeStamp.get(i)), new KeyValue(rectTemp.yProperty(),-this.DBL_KEY_WIDTH)),
                        new KeyFrame(Duration.millis(r.arrStrTimeStamp.get(i)+3000.0D), new KeyValue(rectTemp.yProperty(),500.0D)));
                paneShowNotesUI.getChildren().add(rectTemp);
            }
            t_blocks.play();
            t_blocks.setOnFinished(eh->{
                paneShowNotesUI.getChildren().clear();
            });
            r.tl_recorded.setDelay(Duration.millis(3000.0D*(440.0D/this.paneShowNotesUI.getHeight())));
            r.tl_recorded.playFromStart();
            if(tl_blink.getStatus() == Timeline.Status.RUNNING) tl_blink.stop();
            this.txtStatus.setOpacity(1.0D);
            this.txtStatus.setText("Playing...");
            r.tl_recorded.setOnFinished(eh->{
                this.txtStatus.setText("Ready");
                btnRecord.setDisable(false);
                btnStop.setDisable(true);
                btnPause.setDisable(true);
                btnPlay.setDisable(false);
                btnResume.setDisable(true);
            });
        });
        btnPlay.setLayoutX(240.0D);
        btnPlay.setLayoutY(80.0D);
        dp.getChildren().addAll(btnRecord, btnStop, btnPlay, btnPause, btnResume, p, t_title);
        return dp;
    }

    private Pane getTitleBar() {
        Pane paneTitleBar = new Pane();
        paneTitleBar.setBackground(new Background(new BackgroundFill(Color.web("#0f187a"), CornerRadii.EMPTY, Insets.EMPTY)));
        final double HEIGHT = 32.0D;
        paneTitleBar.setMaxSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneTitleBar.setMinSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneTitleBar.setPrefSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneTitleBar.setLayoutX(0.0D);
        paneTitleBar.setLayoutY(0.0D);
        Text txt_title = new Text("PIANO");
        txt_title.setFont(Font.font("Century Gothic", FontWeight.BOLD, FontPosture.REGULAR, 24));
        txt_title.setLayoutX(10.0D);
        txt_title.setLayoutY(26.0D);
        txt_title.setFill(Color.web("#0fb8d6"));
        Rectangle rectExit = new Rectangle();
        rectExit.setWidth(HEIGHT-4.0D);
        rectExit.setHeight(HEIGHT-8.0D);
        rectExit.setLayoutX(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width-(HEIGHT+2.0D));
        rectExit.setLayoutY(4.0D);
        rectExit.setFill(Color.RED);
        rectExit.setOnMouseReleased(e-> {
            Platform.exit();
        });
        Line l1 = new Line(rectExit.getLayoutX() + 5.0D, rectExit.getLayoutY() + 5.0D, java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - 11.0D, rectExit.getHeight() - 3.0D);
        l1.setMouseTransparent(true);
        l1.setStrokeWidth(3.0D);
        l1.setStroke(Color.WHITE);
        Line l2 = new Line(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - 11.0D, rectExit.getLayoutY() + 5.0D, rectExit.getLayoutX() + 5.0D, rectExit.getHeight() - 3.0D);
        l2.setMouseTransparent(true);
        l2.setStrokeWidth(3.0D);
        l2.setStroke(Color.WHITE);
        rectExit.setOnMouseEntered(e-> {
            rectExit.setFill(Color.BROWN);
            l1.setStroke(Color.YELLOW);
            l2.setStroke(Color.YELLOW);
        });
        rectExit.setOnMouseExited(e-> {
            rectExit.setFill(Color.RED);
            l1.setStroke(Color.WHITE);
            l2.setStroke(Color.WHITE);
        });
        paneTitleBar.getChildren().addAll(txt_title, rectExit,l1,l2);
        return paneTitleBar;
    }

    private Pane getStatusBar() {
        Pane paneStatusBar = new Pane();
        paneStatusBar.setBackground(new Background(new BackgroundFill(Color.web("#424241"), CornerRadii.EMPTY, Insets.EMPTY)));
        final double HEIGHT = 36.0D;
        paneStatusBar.setMaxSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneStatusBar.setMinSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneStatusBar.setPrefSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT);
        paneStatusBar.setLayoutX(0.0D);
        paneStatusBar.setLayoutY(java.awt.Toolkit.getDefaultToolkit().getScreenSize().height-HEIGHT);
        txtStatus.setFont(Font.font("Century Gothic", FontWeight.BOLD, FontPosture.REGULAR, 24));
        txtStatus.setLayoutX(10.0D);
        txtStatus.setLayoutY(26.0D);
        txtStatus.setFill(Color.WHITE);
        paneStatusBar.getChildren().add(txtStatus);
        return paneStatusBar;
    }

    private class Recorder {

        private Timeline tl_counter = new Timeline(), tl_recorded = new Timeline();
        private double dblRecordMS = 0.0D;
        public boolean onrecord = false;
        public ArrayList<Double> arrStrTimeStamp;
        private ArrayList<Integer> arrStrKeyID;

        public Recorder() {
            arrStrTimeStamp = new ArrayList<Double>();
            arrStrKeyID = new ArrayList<Integer>();
            tl_counter.getKeyFrames().add(new KeyFrame(Duration.millis(10.0D), e -> {
                dblRecordMS += 10.0D;
            }));
            tl_counter.setCycleCount(Timeline.INDEFINITE);
        }

        public void startRecord() {
            tl_recorded.getKeyFrames().clear();
            arrStrTimeStamp.clear();
            arrStrKeyID.clear();
            onrecord = true;
            dblRecordMS = 0.0D;
            tl_counter.playFromStart();
            if(tl_blink.getStatus() == Timeline.Status.RUNNING) tl_blink.stop();
            tl_blink.setCycleCount(Timeline.INDEFINITE);
            txtStatus.setText("Recording...");
            tl_blink.play();
        }

        public void stopRecord() {
            onrecord = false;
            tl_counter.stop();
            if(tl_blink.getStatus() == Timeline.Status.RUNNING) tl_blink.stop();
            tl_blink.setOnFinished(e-> {
                txtStatus.setText("Ready");
                txtStatus.setOpacity(1.0D);
            });
            tl_blink.setCycleCount(3);
            txtStatus.setText("Recording Stopped");
            tl_blink.playFromStart();
            /*for(int i = 0; i < arrStrKeyID.size(); i++) {
                System.out.println("Duration = " + arrStrTimeStamp.get(i) + ", key value = " + arrStrKeyID.get(i));
            }*/
        }

        public void pauseRecord() {
            onrecord = false;
            tl_counter.pause();
            if(tl_blink.getStatus() == Timeline.Status.RUNNING) tl_blink.stop();
            tl_blink.setOnFinished(e->{
                txtStatus.setOpacity(1.0D);
            });
            tl_blink.setCycleCount(3);
            txtStatus.setText("Recording PAUSED...");
            tl_blink.playFromStart();
        }

        public void resumeRecord() {
            onrecord = true;
            tl_counter.play();
            if(tl_blink.getStatus() == Timeline.Status.RUNNING) tl_blink.stop();
            tl_blink.setCycleCount(Timeline.INDEFINITE);
            txtStatus.setText("Recording...");
            tl_blink.playFromStart();
        }
    }

    private class PianoKey {

        public Rectangle rect = new Rectangle();
        private final Timeline t = new Timeline();
        private final Paint CLR_PRESSED, CLR_NORMAL;

        public PianoKey(final double WIDTH, final double HEIGHT, final double BORDER_THICKNESS, final Paint CLR_NORMAL, final Paint CLR_HOVER, final Paint CLR_PRESSED) {
            this.CLR_PRESSED = CLR_PRESSED;
            this.CLR_NORMAL = CLR_NORMAL;
            rect.setWidth(WIDTH);
            rect.setHeight(HEIGHT);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(BORDER_THICKNESS);
            rect.setFill(CLR_NORMAL);
            rect.setOnMouseEntered(e -> {
                rect.setFill(CLR_HOVER);
            });
            rect.setOnMouseExited(e -> {
                rect.setFill(CLR_NORMAL);
            });
        }

        public final void showKeyPressedEffect() {
            if (t.getStatus() == Timeline.Status.RUNNING) {
                t.stop();
            }
            t.getKeyFrames().clear();
            t.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(rect.fillProperty(), CLR_PRESSED)),
                    new KeyFrame(Duration.millis(200D), new KeyValue(rect.fillProperty(), CLR_NORMAL)));
            t.playFromStart();
        }
    }

    private final void playNote(MediaPlayer mp) {
        Platform.runLater(() -> {
            MediaPlayer temp = new MediaPlayer(mp.getMedia());
            temp.play();
        });
    }

    private class DraggablePane extends Pane {

        private double locX, locY;

        public DraggablePane(final double WIDTH, final double HEIGHT, final String strBorderColor, final String strPaneColor) {
            this.setBackground(new Background(new BackgroundFill(Color.web(strPaneColor), CornerRadii.EMPTY, Insets.EMPTY)));
            this.setMinSize(WIDTH, HEIGHT);
            this.setMaxSize(WIDTH, HEIGHT);
            this.setPrefSize(WIDTH, HEIGHT);
            final double STROKE_THICKNESS = 3.0D;
            Line l_left = getBorderLine(0.0D, 0.0D, 0.0D, HEIGHT - STROKE_THICKNESS, STROKE_THICKNESS, Color.web(strBorderColor));
            Line l_right = getBorderLine(WIDTH - STROKE_THICKNESS, 0.0D, WIDTH - STROKE_THICKNESS, HEIGHT - STROKE_THICKNESS, STROKE_THICKNESS, Color.web(strBorderColor));
            Line l_top = getBorderLine(0.0D, 0.0D, WIDTH - STROKE_THICKNESS, 0.0D, STROKE_THICKNESS, Color.web(strBorderColor));
            Line l_bottom = getBorderLine(0.0D, HEIGHT, WIDTH - STROKE_THICKNESS, HEIGHT, STROKE_THICKNESS, Color.web(strBorderColor));
            this.getChildren().addAll(l_left, l_right, l_top, l_bottom);
        }

        private final Line getBorderLine(final double START_X, final double START_Y, final double END_X, final double END_Y, final double STROKE_WIDTH, final Color CLR) {
            Line l = new Line(START_X, START_Y, END_X, END_Y);
            l.setFill(CLR);
            l.setStroke(CLR);
            l.setStrokeWidth(STROKE_WIDTH);
            l.setOnMousePressed((MouseEvent e) -> {
                locX = e.getX();
                locY = e.getY();
            });
            l.setOnMouseDragged((MouseEvent event) -> {
                if (event.getSceneX() > 20.0D && event.getSceneY() > 20.0D) {
                    this.setLayoutX(event.getSceneX() - locX);
                    this.setLayoutY(event.getSceneY() - locY);
                }
            });
            return l;
        }
    }

    private class PianoKeyNotes {

        public final MediaPlayer[] sfx_key;

        public PianoKeyNotes() {
            sfx_key = new MediaPlayer[12];
            short resouceFileNumber = 1;
            for (int i = 0; i < 12; i++) {
                String strResourcePath = "";
                if (resouceFileNumber < 10) {
                    strResourcePath = "piano notes/key0" + resouceFileNumber + ".mp3";
                } else {
                    strResourcePath = "piano notes/key" + resouceFileNumber + ".mp3";
                }
                Media sfx_m = new Media(getClass().getResource(strResourcePath).toString());
                sfx_key[i] = new MediaPlayer(sfx_m);
                resouceFileNumber += 2;
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
