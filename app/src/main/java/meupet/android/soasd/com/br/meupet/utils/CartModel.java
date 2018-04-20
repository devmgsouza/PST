package meupet.android.soasd.com.br.meupet.utils;


/**
 * Created by SOA - Development on 06/03/2018.
 */

public class CartModel  {
    String p = "";
    int pk_cart;
    int qtd;
    String codigo_validador = "";

    public String getCodigo_validador() {
        return codigo_validador;
    }

    public void setCodigo_validador(String codigo_validador) {
        this.codigo_validador = codigo_validador;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public int getPk_cart() {
        return pk_cart;
    }

    public void setPk_cart(int pk_cart) {
        this.pk_cart = pk_cart;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }
}
