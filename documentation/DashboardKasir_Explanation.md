# Penjelasan Kode: `DashboardKasir.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [DashboardKasir.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/DashboardKasir.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Kasir Pintar di Toko OriTeh Sapuro**
>
> Bayangkan `DashboardKasir` adalah sebuah **Mesin POS (Point of Sale)** kasir supermarket modern:
> - **Keranjang Belanja (`cart`):** Kantong belanja plastik fisik. Ketika kasir memilih varian teh di menu (`cbVarianProduk`) dan menekan **Tambah**, teh dimasukkan ke dalam kantong. Jika produk yang sama ditambahkan lagi, kasir tidak mengambil kantong baru, melainkan hanya menambahkan jumlah gelas teh di dalam kantong yang sama.
> - **Numeric Pad Tombol 0-9:** Layar sentuh cepat pada mesin kasir untuk menginput kuantitas pesanan dengan cepat menggunakan jari tanpa harus mengetik di keyboard komputer.
> - **Dynamic Change Calculation (`updateKembalian`):** Kalkulator otomatis pada mesin kasir. Begitu kasir menerima uang lembaran dari pembeli dan mengetikkannya, layar langsung menunjukkan uang kembalian secara *real-time* sebelum struk dicetak.
> - **SQL Transaction (Commit & Rollback):** Kasir mencatat penjualan di buku kasir utama (`transaksi`) dan merinci isi belanjaan di lembar lampiran detail (`detail_transaksi`). 
>   - Jika semua barang berhasil dicatat dan uang pas, kasir membubuhkan stempel resmi **"LUNAS" (Commit)**.
>   - Jika di tengah proses pencatatan tiba-tiba printer macet atau mati listrik, kasir akan menyobek lembaran coretan transaksi tersebut dan membatalkan seluruh transaksi **(Rollback)** agar tidak ada laporan palsu/gantung di buku keuangan.

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
      [ cbVarianProduk ]        [ NumPad (0-9) ]
       (Pilih Varian)          (Input Kuantitas)
              |                        |
              +-----------+------------+
                          v
                 [ btnTambahPesanan ]
                          |
              +-----------+-----------+
              | Di keranjang (cart)?  |
              +-----+-----------+-----+
                    |           |
                  (Ya)        (Tidak)
                    |           |
                    v           v
           Jumlah Bertambah    Buat Objek Transaksi baru
           dan Subtotal Update  di List<Transaksi>
                    |           |
                    +-----+-----+
                          |
                          v
                 [ refreshCartTable ]
            - Render ke tabelBelanjaan (JTable)
            - Jumlahkan Total Bayar
                          |
             (Ketik nominal uang pembeli)
                          v
                 [ tfInputNominalBayar ]
            - Dipicu DocumentListener
            - Hitung: Kembalian = Bayar - Total
            - Render kembalian di layar secara real-time
                          |
                 [ btnSimpanTransaksi ]
                          |
                (Mulai DB Transaction)
                - conn.setAutoCommit(false)
                          |
            +-------------+-------------+
            |                           |
            v                           v
     [ INSERT transaksi ]     [ INSERT detail_transaksi ]
     (1 baris header nota)    (Loop detail produk di cart)
            |                           |
            +-------------+-------------+
                          |
                 /=================\
                /   Apakah Sukses   \
               <     Tanpa Error?    >
                \                   /
                 \=================/
                  /               \
              (Sukses)         (Error)
                /                   \
               v                     v
          [ conn.commit() ]     [ conn.rollback() ]
       Tulis permanen ke DB    Batalkan seluruh transaksi
```

---

## 3. Struktur Variabel State & Inisialisasi
Halaman kasir ini mengelola status (*state*) transaksi menggunakan beberapa variabel utama:
* **`produkList`** (`java.util.List<Models.Produk>`): Tempat menyimpan cache data menu teh aktif yang ditarik dari database MySQL agar tidak perlu berulang kali melakukan query database saat memilih produk.
* **`cart`** (`java.util.List<Models.Transaksi>`): Keranjang belanja sementara untuk menampung item yang sedang dipesan sebelum disimpan permanen ke database.
* **`activeUserId`** & **`activeUserName`**: Menyimpan data kasir yang sedang login untuk keperluan audit database (siapa yang melayani transaksi ini).

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor Utama & Setup Listener
```java
    public DashboardKasir() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        loadProducts();
        generateNoNota();
        refreshCartTable();
```
* **`setDefaultCloseOperation(DISPOSE_ON_CLOSE)`** (Baris 26): Mengatur agar ketika jendela kasir ditutup, sistem hanya menghancurkan frame kasir ini saja tanpa mematikan seluruh program (karena mungkin Owner/User lain masih berjalan di background).
* **`loadProducts();`** (Baris 28): Menarik seluruh menu teh aktif dari database untuk dimasukkan ke ComboBox.
* **`generateNoNota();`** (Baris 29): Membuat kode transaksi otomatis unik hari ini.
* **`refreshCartTable();`** (Baris 30): Mereset tampilan tabel belanjaan menjadi kosong di awal.

---

### B. Implementasi Numeric Pad Dinamis
```java
        java.awt.event.ActionListener numPadListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                javax.swing.JButton btn = (javax.swing.JButton) e.getSource();
                appendJumlah(btn.getText());
            }
        };
        btnJumlahPembelian1.addActionListener(numPadListener);
        // ... (tombol 2 s.d 0)
```
* **`numPadListener`** (Baris 33-39): Daripada membuat listener terpisah untuk 10 tombol angka (0-9), kode ini menerapkan prinsip **Reusability** dengan membuat satu objek listener tunggal.
* **`e.getSource()`**: Mengidentifikasi tombol mana yang diklik, melakukan casting ke `JButton`, mengambil angka teksnya (`btn.getText()`), dan memanggil fungsi penambah digit `appendJumlah`.

```java
    private void appendJumlah(String digit) {
        String current = tfJumlahPembelianReadOnly.getText().trim();
        if (current.equals("0") || current.isEmpty()) {
            tfJumlahPembelianReadOnly.setText(digit);
        } else {
            tfJumlahPembelianReadOnly.setText(current + digit);
        }
    }
```
* **`appendJumlah(digit)`** (Baris 159-166): Menangani pengetikan angka. Jika teks saat ini masih kosong atau "0", ganti langsung dengan angka baru. Jika sudah ada angka (misal "1"), tempelkan di belakangnya (menjadi "12"), persis seperti cara kerja tombol kalkulator.

---

### C. Menghubungkan Event Perubahan Teks (Dynamic Kembalian)
```java
        tfInputNominalBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateKembalian(); }
        });
```
* **`DocumentListener`** (Baris 52-56): Mendeteksi setiap perubahan karakter teks di kolom "Nominal Bayar". Baik saat user mengetik angka (`insertUpdate`), menghapus angka (`removeUpdate`), maupun memodifikasi teks (`changedUpdate`), kalkulator kembalian akan langsung dipicu secara otomatis tanpa perlu menekan tombol Enter.

```java
    private void updateKembalian() {
        String bayarStr = tfInputNominalBayar.getText().trim();
        String totalStr = labelPlaceHolderTotalBayar.getText().trim();
        // ...
        try {
            double bayar = Double.parseDouble(bayarStr);
            double total = Double.parseDouble(totalStr);
            double kembalian = bayar - total;
            labelPlaceHolderKembalian.setText(String.format("%.0f", kembalian));
        } catch (NumberFormatException e) {
            labelPlaceHolderKembalian.setText("0");
        }
    }
```
* **`Double.parseDouble()`** (Baris 208-209): Mengubah teks nominal bayar dan total belanja dari string menjadi angka desimal (`double`).
* **`bayar - total`**: Menghitung selisih uang. Hasil kembalian langsung diformat tanpa desimal belakang koma (`%.0f`) dan ditampilkan pada label kembalian di layar.

---

### D. Operasi Keranjang Belanja (Create, Read, Update, Delete)

#### 1) Menambah Pesanan ke Keranjang
```java
    private void btnTambahPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = cbVarianProduk.getSelectedIndex();
        // ...
        Models.Produk p = produkList.get(selectedIndex);
        String qtyStr = tfJumlahPembelianReadOnly.getText().trim();
        int qty = Integer.parseInt(qtyStr); // default ke 1 jika kosong

        boolean found = false;
        for (Models.Transaksi t : cart) {
            if (t.getKodeBarang().equals(p.getKodeProduk())) {
                t.setJumlah(t.getJumlah() + qty);
                t.setSubTotal(t.getJumlah() * t.getHargaSatuan());
                found = true;
                break;
            }
        }
```
* **Pencegahan Duplikasi:** Program melakukan perulangan (*looping*) pada list `cart`. Jika kode produk yang ditambahkan sudah ada di keranjang (`t.getKodeBarang().equals(p.getKodeProduk())`), program hanya mengupdate jumlah kuantitas (`setJumlah`) dan menghitung ulang subtotal tanpa membuat baris baru di tabel.

```java
        if (!found) {
            Models.Transaksi t = new Models.Transaksi(
                p.getKodeProduk(), p.getNamaProduk(), p.getHargaProduk(), qty, qty * p.getHargaProduk()
            );
            cart.add(t);
        }
        refreshCartTable();
```
* Jika produk belum ada di keranjang (`found == false`), buat objek transaksi detail baru (`new Models.Transaksi`) dan masukkan ke list `cart`, kemudian perbarui tabel visual.

#### 2) Mengedit Kuantitas Item
```java
    private void btnEditPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tabelBelanjaan.getSelectedRow();
        // ... ambil jumlah baru dari tfJumlahPembelianReadOnly
        Models.Transaksi t = cart.get(selectedRow);
        t.setJumlah(qty);
        t.setSubTotal(qty * t.getHargaSatuan());
        refreshCartTable();
    }
```
* **`tabelBelanjaan.getSelectedRow()`** (Baris 262): Mencari baris indeks tabel mana yang sedang diklik/dipilih oleh kasir. Data transaksi di indeks tersebut diubah kuantitasnya sesuai angka numpad yang baru diinput.

#### 3) Menghapus Item dari Keranjang
```java
    private void btnHapusPesananActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tabelBelanjaan.getSelectedRow();
        // ...
        cart.remove(selectedRow);
        refreshCartTable();
    }
```
* **`cart.remove(selectedRow)`** (Baris 301): Menghapus item dari daftar list keranjang belanja berdasarkan indeks baris tabel, lalu menyegarkan tampilan tabel.

---

### E. Penyimpanan Transaksi dengan Transaksi Database yang Aman (ACID Transactions)
Bagian paling krusial dari form ini adalah proses penyimpanan belanjaan ke database MySQL. Karena melibatkan dua tabel sekaligus (`transaksi` dan `detail_transaksi`), kode ini menggunakan transaksi database manual untuk menjamin konsistensi data.

```java
        java.sql.Connection conn = null;
        try {
            conn = Models.Koneksi.getConnection();
            conn.setAutoCommit(false); // MEMULAI TRANSAKSI MANUAL
```
* **`conn.setAutoCommit(false);`** (Baris 348): Mematikan mode simpan otomatis database MySQL. Dengan cara ini, query-query INSERT di bawahnya hanya akan disimpan di memori sementara database, belum ditulis permanen ke harddisk.

```java
            String queryTransaksi = "INSERT INTO transaksi (no_nota, bayar, kembalian, total_bayar, id_user) VALUES (?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtT = conn.prepareStatement(queryTransaksi)) {
                // set parameter ...
                stmtT.executeUpdate();
            }
```
* Menyimpan header transaksi (Nomor Nota, total belanja, nominal bayar, kembalian, dan ID kasir) ke tabel `transaksi`.

```java
            String queryDetail = "INSERT INTO detail_transaksi (no_nota, kode_produk, nama_produk, harga_satuan, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmtD = conn.prepareStatement(queryDetail)) {
                for (Models.Transaksi item : cart) {
                    // set parameter ...
                    stmtD.executeUpdate();
                }
            }
```
* Melakukan perulangan untuk mengeksekusi perintah INSERT detail produk sebanyak barang yang ada di keranjang (`cart`) ke tabel `detail_transaksi`.

```java
            conn.commit(); // MENYIMPAN PERMANEN DATA KE DATABASE
            javax.swing.JOptionPane.showMessageDialog(this, "Transaksi Berhasil Disimpan!");
```
* **`conn.commit();`** (Baris 373): Jika proses simpan header dan detail berjalan lancar tanpa error, kirimkan perintah `COMMIT` ke database MySQL agar semua data ditulis secara permanen secara bersamaan.

```java
        } catch (java.sql.SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // MEMBATALKAN SELURUH PERUBAHAN JIKA TERJADI ERROR
                } catch (java.sql.SQLException rollbackEx) { ... }
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + ex.getMessage());
        }
```
* **`conn.rollback();`** (Baris 386): Jika terjadi error database di tengah jalan (misalnya koneksi putus saat memasukkan detail transaksi ke-3), blok `catch` akan memicu `ROLLBACK` untuk membatalkan data header yang sempat di-insert sebelumnya, sehingga database tetap bersih dan terhindar dari data gantung (nota terdaftar tapi tidak memiliki rincian barang).

```java
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException e) { ... }
            }
        }
```
* **`finally`** (Baris 392-401): Blok yang akan selalu dijalankan baik proses sukses maupun gagal, bertugas mengembalikan setelan database ke auto-commit semula dan menutup koneksi database secara aman.
