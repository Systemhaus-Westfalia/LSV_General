package org.shw.lsv.einvoice.fefexfacturaexportacionv1;

import java.util.regex.Pattern;

import org.shw.lsv.einvoice.utils.Direccion;
import org.shw.lsv.einvoice.utils.EDocumentUtils;

public class EmisorFacturaExportacion {
	static final String VALIDATION_RECINTOFISCAL_NOT_NULL = "Documento: Factura de Exportacion, clase: Emisor. Validacion fall??: valor de 'recintoFiscal' de pagos debe ser ='null'";
	static final String VALIDATION_REGIMEN_NOT_NULL       = "Documento: Factura de Exportacion, clase: Emisor. Validacion fall??: valor de 'regimen' de pagos debe ser ='null'";

	String nit;
	String nrc;
	String nombre;
	String codActividad;
	String descActividad;
	String nombreComercial=null;  // null possible
	String tipoEstablecimiento;
    Direccion direccion;
    String telefono;
    String correo;
    String codEstableMH;
    String codEstable;
    String codPuntoVentaMH;
    String codPuntoVenta;
    int tipoItemExpor;
    String recintoFiscal=null;  // null possible
    String regimen=null;  // null possible
    
    
	/**
	 * 
	 */
	public EmisorFacturaExportacion() {
		this.direccion = new Direccion();
	}

/**
 * Validate the Schema conditions
 */
public String validateValues() {
	
	if(getTipoItemExpor()== 2) {
		if ( getRecintoFiscal()!= null)
			return VALIDATION_RECINTOFISCAL_NOT_NULL;
		if ( getRegimen()!= null)
			return VALIDATION_REGIMEN_NOT_NULL;
	}
	// In schema there are more validations, but they are redundant.
	
	return EDocumentUtils.VALIDATION_RESULT_OK;
}

	/**
	 * @return the nit
	 */
	public String getNit() {
		return nit;
	}


	/**
	 * @param nit the nit to set<br>
	 * The parameter is validated.<br>
	 * "pattern" : "^([0-9]{14}|[0-9]{9})$"
	 */
	public void setNit(String nit) {
		final String PATTERN = "^([0-9]{14}|[0-9]{9})$";
		boolean patternOK = (nit!=null) && Pattern.matches(PATTERN, nit);  
		
		if(patternOK)
			this.nit = nit;
		else
	        throw new IllegalArgumentException("Wrong expression 'nit' in FacturaExportacion.Emisor.setNit()");
	}


	/**
	 * @return the nrc
	 */
	public String getNrc() {
		return nrc;
	}


	/**
	 * @param nrc the nrc to set<br>
	 * The parameter is validated.<br>
	 * "pattern" : "^[0-9]{1,8}$"
	 */
	public void setNrc(String nrc) {
		final String PATTERN = "^[0-9]{1,8}$";
		boolean patternOK = (nrc!=null) && Pattern.matches(PATTERN, nrc);  
		
		if(patternOK)
			this.nrc = nrc;
		else
	        throw new IllegalArgumentException("Wrong expression 'nrc' in FacturaExportacion.Emisor.setNrc()");
	}


	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}


	/**
	 * @param nombre the nombre to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 3, "maxLength" : 200
	 */
	public void setNombre(String nombre) {
		final int MINLENGTH = 3;
		final int MAXLENGTH = 200;
		int length = nombre==null?0:nombre.length();
		
		if(length>=MINLENGTH && length<=MAXLENGTH)
			this.nombre = nombre;
		else
	        throw new IllegalArgumentException("Wrong parameter 'nombre' in FacturaExportacion.Emisor.setNombre()");
	}


	/**
	 * @return the codActividad
	 */
	public String getCodActividad() {
		return codActividad;
	}


	/**
	 * @param codActividad the codActividad to set<br>
	 * The parameter is validated.<br>
	 * "pattern" : "^[0-9]{2,6}$"
	 */
	public void setCodActividad(String codActividad) {
		final String PATTERN = "^[0-9]{2,6}$";
		boolean patternOK = (codActividad!=null) && Pattern.matches(PATTERN, codActividad);  
		
		if(patternOK)
			this.codActividad = codActividad;
		else
	        throw new IllegalArgumentException("Wrong expression 'codActividad' in FacturaExportacion.Emisor.setCodActividad()");
	}


	/**
	 * @return the descActividad
	 */
	public String getDescActividad() {
		return descActividad;
	}


	/**
	 * @param descActividad the descActividad to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 1, "maxLength" : 150
	 */
	public void setDescActividad(String descActividad) {
		final int MINLENGTH = 1;
		final int MAXLENGTH = 150;
		int length = descActividad==null?0:descActividad.length();
		
		if(length>=MINLENGTH && length<=MAXLENGTH)
			this.descActividad = descActividad;
		else
	        throw new IllegalArgumentException("Wrong parameter 'descActividad' in FacturaExportacion.Emisor.setDescActividad()");
	}


	/**
	 * @return the nombreComercial
	 */
	public String getNombreComercial() {
		return nombreComercial;
	}


	/**
	 * @param nombreComercial the nombreComercial to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 1, "maxLength" : 150; null also possible
	 */
	public void setNombreComercial(String nombreComercial) {
		final int MINLENGTH = 1;
		final int MAXLENGTH = 150;
		int length = nombreComercial==null?0:nombreComercial.length();
		
		if( (length>=MINLENGTH && length<=MAXLENGTH) || (nombreComercial==null) )
			this.nombreComercial = nombreComercial;
		else
	        throw new IllegalArgumentException("Wrong parameter 'nombreComercial' in FacturaExportacion.Emisor.setNombreComercial()");
	}


	/**
	 * @return the tipoEstablecimiento
	 */
	public String getTipoEstablecimiento() {
		return tipoEstablecimiento;
	}


	/**
	 * @param tipoEstablecimiento the tipoEstablecimiento to set<br>
	 * The parameter is validated.<br>
	 * "enum" : ["01", "02", "04", "07", "20"]
	 */
	public void setTipoEstablecimiento(String tipoEstablecimiento) {
		if (tipoEstablecimiento.compareTo("01")==0 || tipoEstablecimiento.compareTo("02")==0 || tipoEstablecimiento.compareTo("04")==0 || tipoEstablecimiento.compareTo("07")==0 || tipoEstablecimiento.compareTo("20")==0)
			this.tipoEstablecimiento = tipoEstablecimiento;
		else
	        throw new IllegalArgumentException("Wrong parameter 'tipoEstablecimiento' in FacturaExportacion.Emisor.setTipoEstablecimiento()");
	}


	/**
	 * @return the direccion
	 */
	public Direccion getDireccion() {
		return direccion;
	}


	/**
	 * @param direccion the direccion to set
	 */
	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}


	/**
	 * @return the telefono
	 */
	public String getTelefono() {
		return telefono;
	}


	/**
	 * @param telefono the telefono to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 8, "maxLength" : 30
	 */
	public void setTelefono(String telefono) {
		final int MINLENGTH = 8;
		final int MAXLENGTH = 30;
		int length = telefono==null?0:telefono.length();
		
		if(length>=MINLENGTH && length<=MAXLENGTH)
			this.telefono = telefono;
		else
	        throw new IllegalArgumentException("Wrong parameter 'telefono' in FacturaExportacion.Emisor.setTelefono()");
	}


	/**
	 * @return the correo
	 */
	public String getCorreo() {
		return correo;
	}


	/**
	 * @param correo the correo to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 3, "maxLength" : 100
	 */
	public void setCorreo(String correo) {
		final int MINLENGTH = 3;
		final int MAXLENGTH = 100;
		int length = correo==null?0:correo.length();
		
		if(length>=MINLENGTH && length<=MAXLENGTH)
			this.correo = correo;
		else
	        throw new IllegalArgumentException("Wrong parameter 'correo' in FacturaExportacion.Emisor.setCorreo()");
	}


	/**
	 * @return the codEstableMH
	 */
	public String getCodEstableMH() {
		return codEstableMH;
	}


	/**
	 * @param codEstableMH the codEstableMH to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 4, "maxLength" : 4, null also possible
	 */
	public void setCodEstableMH(String codEstableMH) {
		final int MINLENGTH = 4;
		final int MAXLENGTH = 4;
		int length = codEstableMH==null?0:codEstableMH.length();

		if( (length>=MINLENGTH && length<=MAXLENGTH) || (codEstableMH==null) )
			this.codEstableMH = codEstableMH;
		else
	        throw new IllegalArgumentException("Wrong parameter 'codEstableMH' in FacturaExportacion.Emisor.setCodEstableMH()");
	}


	/**
	 * @return the codEstable
	 */
	public String getCodEstable() {
		return codEstable;
	}


	/**
	 * @param codEstable the codEstable to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 1, "maxLength" : 10, null also possible
	 */
	public void setCodEstable(String codEstable) {
		final int MINLENGTH = 1;
		final int MAXLENGTH = 10;
		int length = codEstable==null?0:codEstable.length();

		if( (length>=MINLENGTH && length<=MAXLENGTH) || (codEstable==null) )
			this.codEstable = codEstable;
		else
	        throw new IllegalArgumentException("Wrong parameter 'codEstable' in FacturaExportacion.Emisor.setCodEstable()");
	}


	/**
	 * @return the codPuntoVentaMH 
	 */
	public String getCodPuntoVentaMH() {
		return codPuntoVentaMH;
	}


	/**
	 * @param codPuntoVentaMH the codPuntoVentaMH to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 4, "maxLength" : 4, null also possible
	 */
	public void setCodPuntoVentaMH(String codPuntoVentaMH) {
		final int MINLENGTH = 4;
		final int MAXLENGTH = 4;
		int length = codPuntoVentaMH==null?0:codPuntoVentaMH.length();

		if( (length>=MINLENGTH && length<=MAXLENGTH) || (codPuntoVentaMH==null) )
			this.codPuntoVentaMH = codPuntoVentaMH;
		else
	        throw new IllegalArgumentException("Wrong parameter 'codPuntoVentaMH' in FacturaExportacion.Emisor.setCodPuntoVentaMH()");
	}


	/**
	 * @return the codPuntoVenta
	 */
	public String getCodPuntoVenta() {
		return codPuntoVenta;
	}


	/**
	 * @param codPuntoVenta the codPuntoVenta to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 1, "maxLength" : 15, null also possible
	 */
	public void setCodPuntoVenta(String codPuntoVenta) {
		final int MINLENGTH = 1;
		final int MAXLENGTH = 15;
		int length = codPuntoVenta==null?0:codPuntoVenta.length();

		if( (length>=MINLENGTH && length<=MAXLENGTH) || (codPuntoVenta==null) )
			this.codPuntoVenta = codPuntoVenta;
		else
	        throw new IllegalArgumentException("Wrong parameter 'codPuntoVenta' in FacturaExportacion.Emisor.setCodPuntoVenta()");
	}


    

	/**
	 * @return the tipoItemExpor
	 */
	public int getTipoItemExpor() {
		return tipoItemExpor;
	}


	/**
	 * @param tipoItemExpor the tipoItemExpor to set<br>
	 * The parameter is validated.<br>
	 * "enum" : [1,2,3]
	 */
	public void setTipoItemExpor(int tipoItemExpor) {
		if (tipoItemExpor==1 || tipoItemExpor==2 || tipoItemExpor==3)
			this.tipoItemExpor = tipoItemExpor;
		else
	        throw new IllegalArgumentException("Wrong parameter 'tipoItemExpor' in FacturaExportacion.Emisor.setTipoItemExpor()");
	}


	/**
	 * @return the recintoFiscal
	 */
	public String getRecintoFiscal() {
		return recintoFiscal;
	}


	/**
	 * @param recintoFiscal the recintoFiscal to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 2, "maxLength" : 2
	 */
	public void setRecintoFiscal(String recintoFiscal) {
		final int MINLENGTH = 2;		
		final int MAXLENGTH = 2;
		int length = recintoFiscal==null?0:recintoFiscal.length();
		
		if( (length>=MINLENGTH && length<=MAXLENGTH ) || (recintoFiscal==null) )
			this.recintoFiscal = recintoFiscal;
		else
	        throw new IllegalArgumentException("Wrong parameter 'recintoFiscal' in FacturaExportacion.Emisor.setRecintoFiscal()");
	}


	/**
	 * @return the regimen
	 */
	public String getRegimen() {
		return regimen;
	}


	/**
	 * @param regimen the regimen to set<br>
	 * The parameter is validated.<br>
	 * "minLength" : 1, "maxLength" : 13
	 */
	public void setRegimen(String regimen) {
		final int MINLENGTH = 1;		
		final int MAXLENGTH = 13;
		int length = regimen==null?0:regimen.length();
		
		if( (length>=MINLENGTH && length<=MAXLENGTH ) || (regimen==null) )
			this.regimen = regimen;
		else
	        throw new IllegalArgumentException("Wrong parameter 'regimen' in FacturaExportacion.Emisor.setRegimen()");
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
