/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

/**
 *
 * @author Ardiansyah
 */
public class ProdukForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProdukForm.class.getName());

    /**
     * Creates new form ProdukForm
     */
    public ProdukForm() {
        initComponents();
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                DashboardOwner doPanel = new DashboardOwner();
                frame.setContentPane(doPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
        
        loadData();
        
        tfSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData();
            }
        });
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });
        
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
    }

    private void loadData() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "Kode Produk", "Nama Produk", "Harga Produk"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
        
        String search = tfSearchBar.getText().trim();
        String query = "SELECT * FROM produk";
        if (!search.isEmpty()) {
            query += " WHERE kode_produk LIKE ? OR nama_produk LIKE ?";
        }
        
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            if (!search.isEmpty()) {
                stmt.setString(1, "%" + search + "%");
                stmt.setString(2, "%" + search + "%");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        no++,
                        rs.getString("kode_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga_produk")
                    });
                }
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void tableMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            tfKodeProduk.setText(jTable1.getValueAt(selectedRow, 1).toString());
            tfNamaProduk.setText(jTable1.getValueAt(selectedRow, 2).toString());
            tfHargaProduk.setText(jTable1.getValueAt(selectedRow, 3).toString());
            tfKodeProduk.setEditable(false);
        }
    }
    
    private void clearFields() {
        tfKodeProduk.setText("");
        tfNamaProduk.setText("");
        tfHargaProduk.setText("");
        tfKodeProduk.setEditable(true);
        jTable1.clearSelection();
    }
    
    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        if (kode.isEmpty() || nama.isEmpty() || hargaStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Semua data harus diisi!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = "INSERT INTO produk (kode_produk, nama_produk, harga_produk) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, kode);
            stmt.setString(2, nama);
            stmt.setDouble(3, harga);
            stmt.executeUpdate();
            
            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error tambah produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk yang ingin diedit di tabel!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        if (nama.isEmpty() || hargaStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nama dan Harga tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = "UPDATE produk SET nama_produk = ?, harga_produk = ? WHERE kode_produk = ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nama);
            stmt.setDouble(2, harga);
            stmt.setString(3, kode);
            stmt.executeUpdate();
            
            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil diperbarui!");
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error edit produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk yang ingin dihapus di tabel!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String kode = tfKodeProduk.getText().trim();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk " + kode + "?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            String query = "DELETE FROM produk WHERE kode_produk = ?";
            try (java.sql.Connection conn = Models.Koneksi.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, kode);
                stmt.executeUpdate();
                
                javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
                loadData();
                clearFields();
            } catch (java.sql.SQLException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error hapus produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        tfSearchBar = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfKodeProduk = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfNamaProduk = new javax.swing.JTextField();
        tfHargaProduk = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Manajemen Produk - OriTeh Sapuro");
        jLabel1.setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(179, 179, 179))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Daftar Menu :");

        tfSearchBar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Search :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Kode Produk :");

        tfKodeProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Nama Produk :");

        tfNamaProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        tfHargaProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Harga Produk :");

        btnTambah.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnTambah.setText("Tambah");

        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnEdit.setText("Edit");

        btnHapus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnHapus.setText("Hapus");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tfSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfKodeProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(btnTambah)
                        .addGap(18, 18, 18)
                        .addComponent(btnEdit)
                        .addGap(18, 18, 18)
                        .addComponent(btnHapus))
                    .addComponent(tfHargaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(tfNamaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfKodeProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNamaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfHargaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ProdukForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnTambah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField tfHargaProduk;
    private javax.swing.JTextField tfKodeProduk;
    private javax.swing.JTextField tfNamaProduk;
    private javax.swing.JTextField tfSearchBar;
    // End of variables declaration//GEN-END:variables
}
