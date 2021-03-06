/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entidad;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author admin
 */
@Embeddable
public class DatosGeneralesPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "cod_empr")
    private short codEmpr;
    @Basic(optional = false)
    @NotNull
    @Column(name = "frige_desde")
    @Temporal(TemporalType.TIMESTAMP)
    private Date frigeDesde;

    public DatosGeneralesPK() {
    }

    public DatosGeneralesPK(short codEmpr, Date frigeDesde) {
        this.codEmpr = codEmpr;
        this.frigeDesde = frigeDesde;
    }

    public short getCodEmpr() {
        return codEmpr;
    }

    public void setCodEmpr(short codEmpr) {
        this.codEmpr = codEmpr;
    }

    public Date getFrigeDesde() {
        return frigeDesde;
    }

    public void setFrigeDesde(Date frigeDesde) {
        this.frigeDesde = frigeDesde;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) codEmpr;
        hash += (frigeDesde != null ? frigeDesde.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatosGeneralesPK)) {
            return false;
        }
        DatosGeneralesPK other = (DatosGeneralesPK) object;
        if (this.codEmpr != other.codEmpr) {
            return false;
        }
        if ((this.frigeDesde == null && other.frigeDesde != null) || (this.frigeDesde != null && !this.frigeDesde.equals(other.frigeDesde))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidad.DatosGeneralesPK[ codEmpr=" + codEmpr + ", frigeDesde=" + frigeDesde + " ]";
    }
    
}
