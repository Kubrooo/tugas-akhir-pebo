# Penjelasan Kode: `ProdukForm.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [ProdukForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/ProdukForm.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Papan Daftar Menu dan Kartu Resep Kedai**
>
> Bayangkan `ProdukForm` adalah sebuah **Papan Pengaturan Menu Minuman** yang terletak di dapur **OriTeh Sapuro**:
> - **Tambah Produk (`btnTambah`):** Membuat gantungan papan kayu menu baru dengan menuliskan Kode Unik (misal `OTH-JSM` untuk Jasmine Tea), Nama Teh, dan Harganya, lalu mencantelkannya di dinding daftar menu aktif.
> - **Edit Produk (`btnEdit`):** Menurunkan papan kayu menu yang sudah ada, lalu menulis ulang nama atau harganya yang baru dengan spidol. *Kode Produk dikunci tidak boleh diubah* (`tfKodeProduk.setEditable(false)`) karena itu adalah identitas paten papan kayu tersebut di database.
> - **Pencarian Dinamis (`tfSearchBar`):** Seperti asisten dapur yang dengan cepat memindai papan menu dengan matanya saat diteriaki kata kunci "Es" atau "Jasmine". Papan menu langsung memilah teh yang memiliki kata tersebut.
> - **Hapus Produk / Soft Delete (`btnHapus`):** Jika varian rasa teh tersebut tidak akan dijual lagi, Owner **tidak membakar papan menu lama** tersebut ke tempat sampah. Mengapa? Karena nota penjualan pelanggan bulan lalu yang berisi pesanan teh tersebut akan rusak laporannya jika identitas teh tersebut hilang total dari database. 
>   - Sebagai gantinya, Owner hanya menempelkan label stiker merah bertuliskan **"DIARSIPKAN/HABIS" (`is_deleted = 1`)** pada papan menu tersebut. Papan menu diturunkan dari pajangan depan agar tidak bisa dibeli lagi, namun fisiknya tetap disimpan di lemari arsip gudang (database).

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
  [ tfSearchBar (Ketik Huruf) ]        [ jTable1 (Klik Baris) ]
               |                                 |
        (keyReleased)                    (mouseClicked)
               |                                 |
               v                                 v
        [ loadData() ]                 Pindahkan isi kolom ke:
    - Query: SELECT * FROM produk      - tfKodeProduk (lock)
      WHERE is_deleted = 0             - tfNamaProduk
      AND (nama_produk LIKE ?)         - tfHargaProduk
               |
               +---------------+---------------+
                               |
                               v
                     [ KASUS TOMBOL AKSI ]
                               |
        +----------------------+----------------------+
        | (Tambah)             | (Edit)               | (Hapus)
        v                      v                      v
   [ btnTambah ]          [ btnEdit ]            [ btnHapus ]
   - Validasi input       - Ambil terpilih       - JOptionPane confirm
   - INSERT INTO produk   - UPDATE produk SET    - UPDATE produk SET
     (kode, nama, harga)    nama=?, harga=?        is_deleted = 1
                            WHERE kode=?         (Soft Delete Pattern)
        |                      |                      |
        +----------------------+----------------------+
                               |
                               v
                         [ loadData() ]
                     (Segarkan tabel GUI)
                               |
                               v
                        [ clearFields() ]
                    (Kosongkan kolom input)
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini dirancang untuk operasi CRUD (Create, Read, Update, Delete) produk minuman:
1. `tfKodeProduk` (`JTextField`): Kolom input kode produk (bertindak sebagai Primary Key).
2. `tfNamaProduk` (`JTextField`): Kolom input nama menu teh.
3. `tfHargaProduk` (`JTextField`): Kolom input harga teh per gelas.
4. `tfSearchBar` (`JTextField`): Kolom pencarian dinamis menu produk.
5. `jTable1` (`JTable`): Tabel daftar visual menu produk aktif.
6. `btnTambah`, `btnEdit`, `btnHapus` (`JButton`): Tombol aksi operasi data produk.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor & Navigasi Tutup Jendela
```java
    public ProdukForm() {
        initComponents();
        this.setLocationRelativeTo(null);
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
```
* **`windowClosed(WindowEvent e)`** (Baris 24-32): Sama dengan form laporan, listener ini otomatis mengembalikan Owner ke layar `DashboardOwner` begitu halaman manajemen produk ini ditutup.
* **`loadData();`** (Baris 35): Langsung memanggil fungsi pemuatan tabel produk saat form pertama kali dirender.

---

### B. Pencarian Dinamis Asinkron (On-The-Fly Search)
```java
        tfSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData();
            }
        });
```
* **`keyReleased(KeyEvent evt)`** (Baris 39): Event ini terpicu setiap kali jari pengguna **melepas tombol keyboard** saat mengetik di kolom pencarian. Ini memicu reload fungsi `loadData()` secara dinamis, sehingga tabel produk langsung menyaring data secara *real-time* huruf demi huruf yang diketikkan pengguna tanpa memerlukan tombol cari khusus.

---

### C. Klik Baris Tabel Mengisi Kolom Input (`tableMouseClicked`)
```java
    private void tableMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            tfKodeProduk.setText(jTable1.getValueAt(selectedRow, 1).toString());
            tfNamaProduk.setText(jTable1.getValueAt(selectedRow, 2).toString());
            tfHargaProduk.setText(jTable1.getValueAt(selectedRow, 3).toString());
            tfKodeProduk.setEditable(false);
        }
    }
```
* **`jTable1.getValueAt(selectedRow, Kolom)`** (Baris 113-115): Mengambil teks sel tabel pada baris yang dipilih untuk memindahkannya ke kolom input form (Kode, Nama, Harga).
* **`tfKodeProduk.setEditable(false);`** (Baris 116): **Penting!** Menonaktifkan kemampuan edit pada text field Kode Produk. Karena Kode Produk berfungsi sebagai primary key unik di database MySQL, mengubah kode produk di tengah jalan saat edit akan mengacaukan integritas relasi tabel data transaksi.

---

### D. Operasi CRUD Database

#### 1) Operasi Tambah Data (Create)
```java
    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        // validasi kosong ...
        double harga = Double.parseDouble(hargaStr); // validasi angka ...
        
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
        } catch (java.sql.SQLException ex) { ... }
    }
```
* **`INSERT INTO produk`** (Baris 146): Menyisipkan baris produk baru ke database.
* **`stmt.executeUpdate()`** (Baris 152): Mengeksekusi query manipulasi data (INSERT/UPDATE/DELETE). Setelah sukses, tabel diperbarui (`loadData()`) dan kolom-kolom input dikosongkan kembali (`clearFields()`).

#### 2) Operasi Ubah Data (Update)
```java
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow(); // validasi seleksi tabel ...
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        // validasi input ...
        double harga = Double.parseDouble(hargaStr);
        
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
        } catch (java.sql.SQLException ex) { ... }
    }
```
* **`UPDATE produk SET ... WHERE kode_produk = ?`** (Baris 186): Memperbarui nama dan harga produk di database yang memiliki kode produk yang sama dengan yang dikirimkan.

#### 3) Operasi Hapus Data (Delete - Soft Delete Pattern)
```java
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        // ... validasi seleksi tabel ...
        String kode = tfKodeProduk.getText().trim();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk " + kode + "?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            String query = "UPDATE produk SET is_deleted = 1 WHERE kode_produk = ?";
            try (java.sql.Connection conn = Models.Koneksi.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, kode);
                stmt.executeUpdate();
                
                javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
                loadData();
                clearFields();
            } catch (java.sql.SQLException ex) { ... }
        }
    }
```
* **`JOptionPane.showConfirmDialog`** (Baris 210): Menampilkan kotak konfirmasi Ya/Tidak untuk mencegah ketidaksengajaan terhapusnya menu teh.
* **`UPDATE produk SET is_deleted = 1`** (Baris 213): **Konsep Desain Sangat Penting!** Program tidak menggunakan perintah SQL `DELETE FROM produk` (Hard Delete). Melainkan melakukan **Soft Delete** dengan menyetel bendera (*flag*) `is_deleted` bernilai `1` (true). Dengan cara ini, produk seolah-olah "terhapus" dari pandangan menu kasir dan manajemen produk, namun catatan sejarah transaksi masa lalu yang merujuk pada kode produk ini tidak akan rusak/error karena datanya secara fisik masih ada di database.
