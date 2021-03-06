package bean.listados;

import dao.DepositosFacade;
import dao.ExcelFacade;
import dao.MercaderiasFacade;
import dao.PersonalizedFacade;
import dao.TiposDocumentosFacade;
import dto.LiMercaSinDto;
import entidad.Mercaderias;
import entidad.MercaderiasPK;
import entidad.Depositos;
import entidad.DepositosPK;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import org.primefaces.model.DualListModel;
import util.LlamarReportes;

@ManagedBean
@SessionScoped
public class LiMercaSinCamionesBean {
    
    @EJB
    private DepositosFacade depositosFacade;

    @EJB
    private TiposDocumentosFacade tiposDocumentosFacade;

    @EJB
    private MercaderiasFacade mercaderiasFacade;

    @EJB
    private PersonalizedFacade personalizedFacade;

    @EJB
    private ExcelFacade excelFacade;

    /*private TiposDocumentos tiposDocumentos;
    private List<TiposDocumentos> listaTiposDocumentos;*/
    private List<Mercaderias> listaMercaderias;
    private List<Mercaderias> listaMercaderiasSeleccionadas;

    private DualListModel<Mercaderias> mercaderias;

    private Depositos depositos;
    private List<Depositos> listaDepositos;

    private Date desde;
    private Date hasta;

    private String operacion;
    private String tipoDocumento;

    public LiMercaSinCamionesBean() throws IOException {

    }

    //Operaciones
    //Instanciar objetos
    @PostConstruct
    public void instanciar() {

        /*this.tiposDocumentos = new TiposDocumentos();
        this.listaTiposDocumentos = new ArrayList<TiposDocumentos>();*/
        this.listaMercaderias = new ArrayList<Mercaderias>();
        this.listaMercaderiasSeleccionadas = new ArrayList<Mercaderias>();

        this.depositos = new Depositos(new DepositosPK());
        this.listaDepositos = new ArrayList<Depositos>();

        this.desde = new Date();
        this.hasta = new Date();

        setOperacion("M");
        setTipoDocumento("TODOS");

        //Cities
        List<Mercaderias> mercaSource = new ArrayList<Mercaderias>();
        List<Mercaderias> mercaTarget = new ArrayList<Mercaderias>();

        mercaSource = mercaderiasFacade.listarMercaderiasActivasDepo1();

        mercaderias = new DualListModel<Mercaderias>(mercaSource, mercaTarget);
    }
    
    public List<Depositos> listarDepositos() {
        listaDepositos = depositosFacade.listarDepositosMercaSin();
        return listaDepositos;
    }

    /*public List<TiposDocumentos> listarTiposDocumentosAnudoc() {
        this.listaTiposDocumentos = tiposDocumentosFacade.listarTipoDocumentoLiAnudoc();
        return this.listaTiposDocumentos;
    }*/
    public void ejecutar(String tipo) {

        personalizedFacade.ejecutarSentenciaSQL("drop table tmp_mercaderias");
        personalizedFacade.ejecutarSentenciaSQL("CREATE  TABLE  tmp_mercaderias (cod_merca CHAR(13), cod_barra CHAR(20), \n"
                + "xdesc CHAR(50), nrelacion SMALLINT, cant_cajas integer, cant_unid integer )");

        if (mercaderias.getTarget().size() > 0) {
            listaMercaderiasSeleccionadas = mercaderias.getTarget();

            for (int i = 0; i < listaMercaderiasSeleccionadas.size(); i++) {

                MercaderiasPK pk = listaMercaderiasSeleccionadas.get(i).getMercaderiasPK();

                Mercaderias aux = new Mercaderias();
                aux = mercaderiasFacade.find(pk);
                personalizedFacade.ejecutarSentenciaSQL("INSERT INTO tmp_mercaderias (cod_merca, cod_barra, xdesc, nrelacion,cant_cajas, cant_unid )\n"
                        + "								VALUES ('" + aux.getMercaderiasPK().getCodMerca() + "', '" + aux.getCodBarra() + "', '" + aux.getXdesc() + "', " + aux.getNrelacion() + ",0,0 )");
            }
        } else {
            personalizedFacade.ejecutarSentenciaSQL("insert into tmp_mercaderias\n"
                    + "select m.cod_merca, m.cod_barra, m.xdesc, m.nrelacion, 1, 1\n"
                    + "from  mercaderias m, existencias e\n"
                    + "where m.cod_merca = e.cod_merca\n"
                    + "and m.mestado = 'A'\n"
                    + "and e.cod_depo = 1");
        }

        LlamarReportes rep = new LlamarReportes();

        String fdesde = dateToString(desde);
        String fhasta = dateToString(hasta);
        //String tipoDoc = "";
        String depo = "";

        StringBuilder sql = new StringBuilder();

        /*if (this.tiposDocumentos == null) {
            tipoDoc = "TODOS";
        } else {
            tipoDoc = this.tiposDocumentos.getCtipoDocum();
        }*/
        if (this.depositos == null) {
            depo = "TODOS";
        } else {
            depo = this.depositos.getDepositosPK().getCodDepo() + "";
        }

        sql.append("SELECT m.cod_merca, m.xdesc, e.cod_depo, e.cant_cajas, e.cant_unid, d.xdesc as xdesc_depo \n"
                + "FROM mercaderias m, tmp_mercaderias t, existencias e, depositos d  \n"
                + "WHERE m.cod_empr = 2 and e.cod_empr = 2 and m.mestado = 'A' \n"
                + "AND m.cod_merca = t.cod_merca and m.cod_merca = e.cod_merca \n"
                + "AND e.cod_depo= d.cod_depo \n");

        if (depositos != null) {
            sql.append("AND e.cod_depo = " + depo + " \n");
        }

        sql.append("AND (e.cant_cajas > 0 OR e.cant_unid > 0 ) AND NOT EXISTS ( SELECT *   \n"
                + "FROM movimientos_merca mo  \n"
                + " WHERE  mo.cod_empr = 2 and mo.fmovim BETWEEN '"+fdesde+"' AND '"+fhasta+"'   \n"
                + "AND  mo.ctipo_docum in ('FCO','FCR','CPV') AND e.cod_depo = mo.cod_depo AND e.cod_merca = mo.cod_merca   \n");

        if (depositos != null) {
            sql.append("AND mo.cod_depo= " + depo + " \n");
        }

         sql.append(" ) \n");
        sql.append("ORDER BY m.cod_merca, m.xdesc, e.cod_depo \n");

        System.out.println("SQL limercasincamiones: " + sql.toString());

        if (tipo.equals("VIST")) {
            rep.reporteLiMercaSin2(sql.toString(), dateToString2(desde), dateToString2(hasta), "admin", tipo);
        } else if (tipo.equals("ARCH")) {
            List<LiMercaSinDto> auxExcel = new ArrayList<LiMercaSinDto>();

            auxExcel = excelFacade.listarLiMercaSin(sql.toString());

            rep.excelLiMercaSin2(auxExcel);
        }

    }

    private String dateToString(Date fecha) {

        String resultado = "";

        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            resultado = dateFormat.format(fecha);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atencion", "Error al convertir fecha"));
        }

        return resultado;
    }

    private String dateToString2(Date fecha) {

        String resultado = "";

        try {

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            resultado = dateFormat.format(fecha);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atencion", "Error al convertir fecha"));
        }

        return resultado;
    }

    //Getters & Setters
    /*public TiposDocumentos getTiposDocumentos() {
        return tiposDocumentos;
    }

    public void setTiposDocumentos(TiposDocumentos tiposDocumentos) {
        this.tiposDocumentos = tiposDocumentos;
    }

    public List<TiposDocumentos> getListaTiposDocumentos() {
        return listaTiposDocumentos;
    }

    public void setListaTiposDocumentos(List<TiposDocumentos> listaTiposDocumentos) {
        this.listaTiposDocumentos = listaTiposDocumentos;
    }*/
    public Date getDesde() {
        return desde;
    }

    public void setDesde(Date desde) {
        this.desde = desde;
    }

    public Date getHasta() {
        return hasta;
    }

    public void setHasta(Date hasta) {
        this.hasta = hasta;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public Depositos getDepositos() {
        return depositos;
    }

    public void setDepositos(Depositos depositos) {
        this.depositos = depositos;
    }

    public List<Depositos> getListaDepositos() {
        return listaDepositos;
    }

    public void setListaDepositos(List<Depositos> listaDepositos) {
        this.listaDepositos = listaDepositos;
    }


    /*public List<Mercaderias> getListaMercaderias() {
        return listaMercaderias;
    }

    public void setListaMercaderias(List<Mercaderias> listaMercaderias) {
        this.listaMercaderias = listaMercaderias;
    }

    public List<Mercaderias> getListaMercaderiasSeleccionadas() {
        return listaMercaderiasSeleccionadas;
    }

    public void setListaMercaderiasSeleccionadas(List<Mercaderias> listaMercaderiasSeleccionadas) {
        this.listaMercaderiasSeleccionadas = listaMercaderiasSeleccionadas;
    }*/
    public DualListModel<Mercaderias> getMercaderias() {
        return mercaderias;
    }

    public void setMercaderias(DualListModel<Mercaderias> mercaderias) {
        this.mercaderias = mercaderias;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

}
