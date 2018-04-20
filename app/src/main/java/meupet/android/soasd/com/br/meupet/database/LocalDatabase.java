package meupet.android.soasd.com.br.meupet.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Adapter;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.com.soasd.meupet.Pet;
import br.com.soasd.meupet.Produto;
import br.com.soasd.meupet.Raca;
import meupet.android.soasd.com.br.meupet.utils.CartModel;
import meupet.android.soasd.com.br.meupet.utils.SettingsModel;

/**
 * Created by SOA - Development on 28/02/2018.
 */

public class LocalDatabase {
    private SQLiteDatabase db;
    private CreateBase nb;

    public LocalDatabase(Context context){
        nb = new CreateBase(context);
    }


    public void addItemCart(String gson, int qtd, String codigo_pet) {
        ContentValues values = new ContentValues();
        values.put("GSON_ITEM", gson);
        values.put("QTD",qtd);
        values.put("CODIGO_VALIDADOR",codigo_pet);


        db = nb.getWritableDatabase();
        db.insert("CART", null, values);

    }

    public void updateQtd(int qtd, String codigo_produto) {
        ContentValues values = new ContentValues();
        String where = "CODIGO_PRODUTO = " + codigo_produto;
        values.put("QTD",qtd);
        db.update("CART", values, where, null);

    }

    public void deleteItem(int pk_produto) {
        String where = "PK_CART = " + pk_produto;
        db = nb.getWritableDatabase();
        db.delete("CART", where, null);

    }
    public void deleteItemCodigoValidador(String codigo_validador) {
        String where = "CODIGO_VALIDADOR = '" + codigo_validador + "'";
        db = nb.getWritableDatabase();
        db.delete("CART", where, null);

    }

    public void clearCart() {
        String where = "PK_CART > 0";
        db = nb.getWritableDatabase();
        db.delete("CART", where, null);

    }

    public List<CartModel> selectItens() {

        List<CartModel> lista = new ArrayList<>();
        Cursor cursor;
        String[] campos = {"PK_CART", "GSON_ITEM", "QTD", "CODIGO_VALIDADOR"};
        String where = "PK_CART > 0";
        db = nb.getReadableDatabase();

        cursor = db.query("CART", campos, where, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                CartModel c = new CartModel();
                c.setPk_cart(cursor.getInt(cursor.getColumnIndex("PK_CART")));
                c.setP(cursor.getString(cursor.getColumnIndexOrThrow("GSON_ITEM")));
                c.setQtd(cursor.getInt(cursor.getColumnIndex("QTD")));
                c.setCodigo_validador(cursor.getString(cursor.getColumnIndexOrThrow("CODIGO_VALIDADOR")));
                lista.add(c);


            }
        }

        return lista;
    }

    public void addSetting(SettingsModel s) {
        deleteAllSetting();
        ContentValues values = new ContentValues();
        values.put("FONT_SIZE", s.getFonteSize());
        values.put("USER_ID", s.getUserId());
        db = nb.getReadableDatabase();
        db.insert("SETTINGS", null, values);

    }

    public void deleteAllSetting(){
        String where = "PK_SETTING > 0 ";
        db = nb.getReadableDatabase();
        db.delete("SETTINGS", where, null);
    }

    public SettingsModel loadSettings(){
        SettingsModel m = null;
        Cursor cursor;
        String[] campos = {"FONT_SIZE, USER_ID"};
        String where = "PK_SETTING > 0";

       db = nb.getReadableDatabase();

        cursor = db.query("SETTINGS", campos, where, null, null, null, null, null);
        if (cursor.moveToNext() == true) {

                m = new SettingsModel();
                m.setFonteSize(cursor.getInt(cursor.getColumnIndexOrThrow("FONT_SIZE")));
                m.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("USER_ID")));

            }

        return  m;
    }

    public void updateSettings(String userId){
        ContentValues values = new ContentValues();
        String where = "PK_SETTING > 0";
        values.put("USER_ID", userId);
        values.put("FONT_SIZE", 14);
        db = nb.getWritableDatabase();
        db.update("SETTINGS", values, where, null);
    }

    public void inserirPet(String gsonPet, String codigo){
        ContentValues values = new ContentValues();
        values.put("GSON_PET", gsonPet);
        values.put("CODIGO_VALIDADOR", codigo);

        db = nb.getWritableDatabase();
        db.insert("MEUSPETS", null, values);
    }

    public void deletePET(String codigo) {
        deleteItemCodigoValidador(codigo);
        String where = "CODIGO_VALIDADOR = '" + codigo + "'";
        db = nb.getWritableDatabase();
        db.delete("MEUSPETS", where, null);

    }

    public void deleteAllPET() {
        String where = "PK_PET > 0 ";
        db = nb.getWritableDatabase();
        db.delete("MEUSPETS", where, null);

    }

    public List<Pet> selectPets() {

        List<Pet> lista = new ArrayList<>();
        Cursor cursor;
        String[] campos = {"PK_PET", "GSON_PET", "CODIGO_VALIDADOR"};
        String where = "PK_PET > 0";
        db = nb.getReadableDatabase();

        cursor = db.query("MEUSPETS", campos, where, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                String petGson = cursor.getString(cursor.getColumnIndexOrThrow("GSON_PET"));
                Pet p = new Gson().fromJson(petGson, Pet.class);
                lista.add(p);

            }
        }

        return lista;
    }

    public void inserirRaca(Raca raca){
        ContentValues values = new ContentValues();
        values.put("TEXT_RACA", raca.getText_descricao());
        values.put("FK_FAMILIA", raca.getFk_familia());

        db = nb.getWritableDatabase();
        db.insert("RACAS", null, values);
    }

    public void deleteAllRacas(){
        String where = "PK_RACA > 0 ";
        db = nb.getWritableDatabase();
        db.delete("RACAS", where, null);
    }

    public List<String> buscarRacas(int param){
        List<String> lista = new ArrayList<>();
        Cursor cursor;
        String[] campos = {"TEXT_RACA"};
        String where = "FK_FAMILIA = " + param;
        String orderby = "TEXT_RACA ASC";
        db = nb.getReadableDatabase();

        cursor = db.query("RACAS", campos, where, null, null, null, orderby, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                lista.add(cursor.getString(cursor.getColumnIndexOrThrow("TEXT_RACA")));


            }
        }

        return lista;
    }


    private class CreateBase extends SQLiteOpenHelper {
        private static final String NOME_BANCO = "config.db";
        private static final int VERSAO = 12;

        public CreateBase(Context context) {
            super(context, NOME_BANCO, null, VERSAO);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            String SQL = "CREATE TABLE IF NOT EXISTS CART (PK_CART INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "GSON_ITEM TEXT, QTD INTEGER, CODIGO_VALIDADOR TEXT)";
            db.execSQL(SQL);

            SQL = "CREATE TABLE IF NOT EXISTS MEUSPETS (PK_PET INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "GSON_PET TEXT, CODIGO_VALIDADOR TEXT)";
            db.execSQL(SQL);

            SQL = "CREATE TABLE IF NOT EXISTS SETTINGS (PK_SETTING INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "FONT_SIZE INT, USER_ID TEXT)";
            db.execSQL(SQL);

            SQL = "CREATE TABLE IF NOT EXISTS RACAS (PK_RACA INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "TEXT_RACA TEXT, FK_FAMILIA INT)";
            db.execSQL(SQL);

            SettingsModel s = new SettingsModel();
            s.setUserId("0");
            s.setFonteSize(14);

            ContentValues values = new ContentValues();
            values.put("FONT_SIZE", s.getFonteSize());
            values.put("USER_ID", s.getUserId());
            db.insert("SETTINGS", null, values);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion){
                db.execSQL("DROP TABLE IF EXISTS " + "CART");
                onCreate(db);
                db.execSQL("DROP TABLE IF EXISTS " + "SETTINGS");
                onCreate(db);
                db.execSQL("DROP TABLE IF EXISTS " + "MEUSPETS");
                onCreate(db);
                db.execSQL("DROP TABLE IF EXISTS " + "RACAS");
                onCreate(db);
            }

        }
    }

}
