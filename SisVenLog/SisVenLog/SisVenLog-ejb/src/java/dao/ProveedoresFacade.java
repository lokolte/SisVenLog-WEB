/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;


import entidad.Proveedores;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

/**
 *
 * @author admin
 */
@Stateless
public class ProveedoresFacade extends AbstractFacade<Proveedores> {

    @PersistenceContext(unitName = "SisVenLog-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProveedoresFacade() {
        super(Proveedores.class);
    }
    
    public void insertarProveedores(Proveedores proveedores) {

        StoredProcedureQuery q = getEntityManager().createStoredProcedureQuery("InsertarProveedores");
        q.registerStoredProcedureParameter("xnombre", String.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("xruc", String.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("xtelef", String.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("xdirec", String.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("xcontacto", String.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("ilimite_credito", Long.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("cod_contasys", Short.class, ParameterMode.IN);
        
        q.registerStoredProcedureParameter("falta", Date.class, ParameterMode.IN);
        q.registerStoredProcedureParameter("cusuario", String.class, ParameterMode.IN);
        
        q.setParameter("xnombre", proveedores.getXnombre());
        q.setParameter("xruc", proveedores.getXruc());
        q.setParameter("xtelef", proveedores.getXtelef());
        q.setParameter("xdirec", proveedores.getXdirec());
        q.setParameter("xcontacto", proveedores.getXcontacto());
        q.setParameter("ilimite_credito", proveedores.getIlimiteCredito());
        q.setParameter("cod_contasys", proveedores.getCodContasys());
        
        q.setParameter("falta", proveedores.getFalta());
        q.setParameter("cusuario", proveedores.getCusuario());
        
        
        q.execute();

    }
}