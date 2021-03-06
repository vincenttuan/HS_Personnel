package com.hs.personnel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.hs.personnel.dao.ClockOnDao;
import com.hs.personnel.dao.EmployeeDao;
import com.hs.personnel.dao.StatusDao;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import usb.Message;
import usb.USBReader;
import utils.ImageUtil;

/**
 *
 * @author vincenttuan
 */
public class PersonnelJFrame extends javax.swing.JFrame {
    ClockOnDao dao;
    StatusDao statusDao;
    EmployeeDao employeeDao;

    // init block
    {
        dao = new ClockOnDao();
        statusDao = new StatusDao();
        employeeDao = new EmployeeDao();
    }

    class ClockOnArgs {

        String emp_no; // 員工編號
        String status_name; // 打卡時段
        Date clock_on; // 打卡時間
        String image; // 員工快照

        @Override
        public String toString() {
            return "ClockOnArgs{" + "emp_no=" + emp_no + ", status_name=" + status_name + ", clock_on=" + clock_on + ", image=" + image + '}';
        }

    }

    // 建立打卡資訊物件
    private ClockOnArgs clockOnArgs = new ClockOnArgs();

    // 宣告 Webcam (目前 focus 是哪一個 Webcam)
    Webcam webcam;

    // 初始化 Webcam
    private void initWebcam() {
        // 建立 Webcam JPanel 陣列
        JPanel[] jPanelWebcams = new JPanel[Webcam.getWebcams().size()];
        // 依序建立 Webcam JPanel 放在 tabbedPane
        for (int i = 0; i < Webcam.getWebcams().size(); i++) {
            // 放入 JPanel 實體物件
            jPanelWebcams[i] = new JPanel();
            String wname = Webcam.getWebcams().get(i).getName();
            wname = wname.substring(0, 12).trim();
            tabbedPane.addTab(i + ":" + wname, jPanelWebcams[i]);
            showWebcam(i, jPanelWebcams[i]);
        }

        // Webcam 預設
        webcam = Webcam.getDefault();

        // 建立 tabbedPane ChangeListener 監聽器
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 取得 tabbedPane 目前被選取的 tab index
                int index = tabbedPane.getSelectedIndex();
                // 將 webcam 變更為目前所選的攝影機
                webcam = Webcam.getWebcamByName(Webcam.getWebcams().get(index).getName());
                System.out.println(index + ", Tab changed to: " + tabbedPane.getTitleAt(index));
            }
        });

        // 建立 Windows 監聽器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                for (Webcam webcam : Webcam.getWebcams()) {
                    webcam.close();
                }
                e.getWindow().dispose();
            }
        });
    }

    // 顯示 Webcam
    private void showWebcam(int index, JPanel jPanelWebcam) {

        webcam = Webcam.getDefault();
        // 透過 i 值來找到 webcam 的名字用以決定要顯示的 webcam
        //webcam = Webcam.getWebcamByName(Webcam.getWebcams().get(index).getName());

        webcam.setViewSize(WebcamResolution.QVGA.getSize());

        // Webcam 設定
        WebcamPanel webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setDisplayDebugInfo(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(true);

        jPanelWebcam.add(webcamPanel); // 加入 WebcamPanel
        jPanelWebcam.getParent().revalidate(); // 重繪
    }

    // 快照
    private void takePicture() {
        if (webcam == null) {
            return;
        }
        if (!webcam.isOpen()) {
            webcam.open();
        }
        try {
            BufferedImage image = webcam.getImage();
            //ImageUtil.saveImage(image, "sample.png");
            clockOnArgs.image = ImageUtil.imageToBase64String(image);
            image = ImageUtil.resizeImage(image, picture.getWidth(), picture.getHeight());
            image = ImageUtil.mirrorImage(image);
            picture.setIcon(new ImageIcon(image));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 預設時段 
    private void defaultClockOn() {
        clear_btn_status_color();

        List<Map<String, String>> list = statusDao.query();
        System.out.println(list);
        System.out.println(LocalDateTime.now().getHour());

        btn_status_1.setBackground(Color.red);
        btn_status_1.setForeground(Color.yellow);
        clockOnArgs.status_name = btn_status_1.getText();

    }

    private void rfid() {
        try {
            // 封裝 Serial 傳來的訊息
            Message message = new Message() {
                // 顯示所傳來的資料
                @Override
                public void setData(String data) {
                    // rfid 資料 = data 去除 enter 字元
                    String rfid = data.replaceAll("\n", "").trim();
                    // 根據 rfid 取得 emp_no
                    String emp_no = employeeDao.getEmpNo(rfid);
                    // 顯示 emp_no 資料
                    edit_emp_no.setText(emp_no);
                    System.out.println(rfid);
                    // 進行打卡
                    if (emp_no != null && !emp_no.trim().equals("")) {
                        clockOn();
                    }
                }
            };

            // 連接指定的 Serial Port
            (new USBReader()).connect("/dev/cu.wchusbserial14640", message);
        } catch (Exception e) {
        }
    }

    public PersonnelJFrame() {
        initComponents();
        // 初始化 Webcam
        initWebcam();
        // 預設時段
        defaultClockOn();
        // rfid 偵測
        rfid();
        // 各種事件註冊
        setListener();
    }

    private void setListener() {
        btn_status_1.setFocusable(false);
        btn_status_2.setFocusable(false);
        btn_status_3.setFocusable(false);
        btn_status_4.setFocusable(false);
        clickOnJTable.setFocusable(false);
        
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                edit_emp_no.requestFocus();
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        clickOnJTable = new javax.swing.JTable();
        edit_emp_no = new javax.swing.JTextField();
        edit_clock_on = new javax.swing.JTextField();
        btn_status_1 = new javax.swing.JButton();
        btn_status_2 = new javax.swing.JButton();
        btn_status_3 = new javax.swing.JButton();
        btn_status_4 = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        picture = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("打卡系統");

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        jLabel1.setText("員工編號");
        jLabel1.setToolTipText("");

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        jLabel2.setText("打卡時段");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel3.setText("打卡時間");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel4.setText("打卡記錄");

        clickOnJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "員工編號", "員工姓名", "時段(status)", "打卡日期", "打卡時間"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(clickOnJTable);

        edit_emp_no.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        edit_emp_no.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                edit_emp_noKeyPressed(evt);
            }
        });

        edit_clock_on.setEditable(false);
        edit_clock_on.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N

        btn_status_1.setText("上午上班");
        btn_status_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_status_1ActionPerformed(evt);
            }
        });

        btn_status_2.setText("上午下班");
        btn_status_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_status_2ActionPerformed(evt);
            }
        });

        btn_status_3.setText("下午上班");
        btn_status_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_status_3ActionPerformed(evt);
            }
        });

        btn_status_4.setText("下午下班");
        btn_status_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_status_4ActionPerformed(evt);
            }
        });

        jMenu1.setText("功能");

        jMenuItem1.setText("員工資料");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("資料查詢");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("離開系統");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(edit_clock_on))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btn_status_1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btn_status_2))
                                    .addComponent(edit_emp_no, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btn_status_3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btn_status_4))))
                            .addComponent(jLabel4))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(picture, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_status_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btn_status_1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btn_status_3, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                    .addComponent(btn_status_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(edit_emp_no, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(edit_clock_on, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4))
                    .addComponent(tabbedPane))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
                .addGap(47, 47, 47))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_status_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_status_1ActionPerformed
        clear_btn_status_color();
        btn_status_1.setBackground(Color.red);
        btn_status_1.setForeground(Color.yellow);
        clockOnArgs.status_name = btn_status_1.getText();
        
    }//GEN-LAST:event_btn_status_1ActionPerformed

    private void btn_status_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_status_2ActionPerformed
        clear_btn_status_color();
        btn_status_2.setBackground(Color.red);
        btn_status_2.setForeground(Color.yellow);
        clockOnArgs.status_name = btn_status_2.getText();
        
    }//GEN-LAST:event_btn_status_2ActionPerformed

    private void btn_status_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_status_3ActionPerformed
        clear_btn_status_color();
        btn_status_3.setBackground(Color.red);
        btn_status_3.setForeground(Color.yellow);
        clockOnArgs.status_name = btn_status_3.getText();
    }//GEN-LAST:event_btn_status_3ActionPerformed

    private void btn_status_4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_status_4ActionPerformed
        clear_btn_status_color();
        btn_status_4.setBackground(Color.red);
        btn_status_4.setForeground(Color.yellow);
        clockOnArgs.status_name = btn_status_4.getText();
    }//GEN-LAST:event_btn_status_4ActionPerformed

    private void edit_emp_noKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_edit_emp_noKeyPressed
        //setTitle(evt.getKeyCode() + "");
        // 使用者按下 enter 鍵 -> 進行快照 + 打卡程序
        if (evt.getKeyCode() == 10) {
            clockOn();
            edit_emp_no.selectAll();
        }
    }//GEN-LAST:event_edit_emp_noKeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EmployeeJFrame().setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReportJFrame().setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        dispose();
        System.exit(0);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    // 打卡方法
    private void clockOn() {
        // 照片快照
        takePicture();
        // 員工編號
        clockOnArgs.emp_no = edit_emp_no.getText();
        // 打卡時間
        clockOnArgs.clock_on = new Date();
        edit_clock_on.setText(clockOnArgs.clock_on.toString());

        // 印出 clockOnArgs
        System.out.println(clockOnArgs);

        // 存入資料庫
        int flag = dao.add(clockOnArgs.emp_no, clockOnArgs.status_name, clockOnArgs.image);
        System.out.println("flag: " + flag);

        if (flag > 0) {
            // 加入到 jTable
            DefaultTableModel model = (DefaultTableModel) clickOnJTable.getModel();
            model.setRowCount(0); // 清空 jTable
            List<Map<String, String>> list = dao.queryToday(clockOnArgs.emp_no);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss");
            for (Map<String, String> map : list) {
                Date clock_on = null;
                try {
                    // String 轉 Date
                    clock_on = sdf.parse(map.get("clock_on"));
                } catch (Exception e) {
                }
            
                Object[] rowData = {
                    map.get("emp_no"),
                    map.get("emp_name"),
                    map.get("status_name"),
                    sdf_date.format(clock_on),
                    sdf_time.format(clock_on)
                };
                model.addRow(rowData);
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "打卡錯誤：" + flag);
        }

    }

    private void clear_btn_status_color() {
        btn_status_1.setBackground(Color.LIGHT_GRAY);
        btn_status_1.setForeground(Color.black);
        btn_status_2.setBackground(Color.LIGHT_GRAY);
        btn_status_2.setForeground(Color.black);
        btn_status_3.setBackground(Color.LIGHT_GRAY);
        btn_status_3.setForeground(Color.black);
        btn_status_4.setBackground(Color.LIGHT_GRAY);
        btn_status_4.setForeground(Color.black);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PersonnelJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PersonnelJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PersonnelJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PersonnelJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PersonnelJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_status_1;
    private javax.swing.JButton btn_status_2;
    private javax.swing.JButton btn_status_3;
    private javax.swing.JButton btn_status_4;
    private javax.swing.JTable clickOnJTable;
    private javax.swing.JTextField edit_clock_on;
    private javax.swing.JTextField edit_emp_no;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel picture;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
