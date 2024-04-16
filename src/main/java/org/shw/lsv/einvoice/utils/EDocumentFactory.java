/**
 * 
 */
package org.shw.lsv.einvoice.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import org.adempiere.core.domains.models.I_C_Invoice;
import org.adempiere.core.domains.models.X_C_Region;
import org.adempiere.core.domains.models.X_E_Activity;
import org.adempiere.core.domains.models.X_E_BPType;
import org.adempiere.core.domains.models.X_E_DocType;
import org.adempiere.core.domains.models.X_E_Duties;
import org.adempiere.core.domains.models.X_E_Enviroment;
import org.adempiere.core.domains.models.X_E_PlantType;
import org.adempiere.core.domains.models.X_E_Recipient_Identification;
import org.adempiere.core.domains.models.X_E_TimeSpan;
import org.compiere.model.MBPartner;
import org.compiere.model.MCity;
import org.compiere.model.MClient;
import org.compiere.model.MCountry;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MTax;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.json.JSONArray;
import org.json.JSONObject;
import org.shw.lsv.einvoice.anulacionv2.Anulacion;
import org.shw.lsv.einvoice.feccfcreditofiscalv3.CreditoFiscal;

/**
 * 
 */
public abstract class EDocumentFactory {
	public abstract void generateJSONInputData();
	public abstract StringBuffer getEDocumentErrorMessages(); 
	public abstract EDocument generateEDocument();
	public abstract String createJsonString() throws Exception;

	protected MClient	client = null;
	protected MOrgInfo orgInfo = null;
	protected String trxName;
	protected Properties contextProperties;
	protected JSONObject jsonInputToFactory;  // Will contain data passed to factory
	protected Language languageUsed = null;
	
	public static String sqlCuerpoDocumento 		= "SELECT * FROM	EI_Invoiceline_Cumm "
														+ " WHERE c_invoice_id = ? ";
	public static String sqlApendice 				= "SELECT (invoiceinfo) as invoiceinfo FROM shw_c_invoice_header_vt i"
														+ " WHERE AD_LANGUAGE = 'es_SV' AND C_Invoice_ID=?";
	
	public static int	lineNo 										= 50;
	public static String CUERPODOCUMENTO_VENTANOSUJETO				= "ventanosuj";
	public static String CUERPODOCUMENTO_VENTAEXENTA				= "ventaex";
	public static String CUERPODOCUMENTO_VENTAGRAVADA				= "ventagravada";
	public static String CUERPODOCUMENTO_VENTANOGRAVADA				= "cuentaajena";
	public static String CUERPODOCUMENTO_PRODUCTVALUE				= "productvalue";
	public static String CUERPODOCUMENTO_PRODUCTNAME				= "name";
	public static String CUERPODOCUMENTO_PRICEACTUAL				= "priceactual";
	public static String CUERPODOCUMENTO_QTYINVOICED				= "qtyinvoiced";
	public static String CUERPODOCUMENTO_LINETOTALAMT   			= "linetotalamt";
	

	public static String TAXINDICATOR_IVA 							= "IVA";
	public static String TAXINDICATOR_NSUJ 							= "NSUJ";
	public static String TAXINDICATOR_EXT 							= "EXT";
	public static String TAXINDICATOR_RET 							= "RET";
	public static String CHARGETYPE_CTAJ	 						= "CTAJ";
	
	public static String Columnname_E_Activity_ID 					= "E_Activity_ID";
	
	
	

	
	
	public EDocumentFactory(String trxName, Properties contextProperties, MClient client, MOrgInfo orgInfo) {
		this.trxName = trxName;
		this.contextProperties = contextProperties;
		this.client = client;
		this.orgInfo = orgInfo;
		String lang = client.getAD_Language();	
		this.languageUsed = Language.getLanguage(lang);
			
			
	}
	
	public String getCodigoGeneracion (String eDocumentAsJsonString) {

        JSONObject eDocumentAsJson  = new JSONObject(eDocumentAsJsonString);
        JSONObject identification = (JSONObject)eDocumentAsJson.get(EDocument.IDENTIFICACION);
        String codigoGeneracion = identification.getString(EDocument.CODIGOGENERACION);
		return codigoGeneracion;
	}
	
	public String getNumeroControl (String eDocumentAsJsonString) {

        JSONObject eDocumentAsJson  = new JSONObject(eDocumentAsJsonString);
        JSONObject identification = (JSONObject)eDocumentAsJson.get(EDocument.IDENTIFICACION);
        String codigoGeneracion = identification.getString(EDocument.NUMEROCONTROL);
		return codigoGeneracion;
	}
	
	public boolean writeToFile (String json, MInvoice invoice, String directory) {
		System.out.println("Anulacion: start writing to file");
		try
		{
			Path rootpath = Paths.get(directory);
			if (!Files.exists(rootpath)) {
				return false;
			}    	

			directory = (directory.endsWith("/")
					|| directory.endsWith("\\"))
					? directory:directory + "/";
			Path path = Paths.get(directory + invoice.getDateAcct().toString().substring(0, 10) + "/");
			Files.createDirectories(path);
			//java.nio.file.Files;
			Files.createDirectories(path);
			String filename = path +"/" + invoice.getDocumentNo().replace(" ", "") + ".json"; 
			File out = new File (filename);
			Writer fw = new OutputStreamWriter(new FileOutputStream(out, false), "UTF-8");
			fw.write(json);
			fw.flush ();
			fw.close ();
			float size = out.length();
			size /= 1024;
			System.out.println("File size: " + out.getAbsolutePath() + " - " + size + " kB");
			System.out.println("Printed To: " + filename);
			System.out.println("Anulacion: end writing to file");
			return true;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	

	public JSONObject generateApendiceInputData(int invoiceID) {
//		String sqlSelect = "SELECT (invoiceinfo) as invoiceinfo FROM shw_c_invoice_header_vt i"
//				+ " WHERE AD_LANGUAGE = 'es_SV' AND C_Invoice_ID=?";
		String infoInvoice = DB.getSQLValueStringEx(null, sqlApendice, invoiceID);
		if (infoInvoice == null || infoInvoice.length()==0)
			infoInvoice = " ";
		ArrayList<String> info = new ArrayList<String>();
		int part = 150;
		if (infoInvoice.length()<= 150) {
			info.add(infoInvoice);
		}
		while(infoInvoice.length() > 150) {
			part = infoInvoice.length()>=150?150:infoInvoice.length();
			String infopart = infoInvoice.substring(0,part);
			info.add(infopart);
			infoInvoice = infoInvoice.substring(part, infoInvoice.length());
			if (info.size() == 10)
				break;
		}
		JSONObject jsonApendice = new JSONObject();
		JSONArray jsonTributosArray = new JSONArray();
		for(int i = 0; i < info.size(); i++) {

			JSONObject jsonApendiceItem = new JSONObject();
			jsonApendiceItem.put(EDocument.CAMPO, "Info");
			jsonApendiceItem.put(EDocument.ETIQUETA, "Descripcion");
			jsonApendiceItem.put(EDocument.VALOR, info.get(i));
			jsonTributosArray.put(jsonApendiceItem);
		}
		
		jsonApendice.put(CreditoFiscal.APENDICE, jsonTributosArray);
		return jsonApendice;
	}
	
	public X_E_Enviroment  client_getE_Enviroment(MClient client) {
		X_E_Enviroment e_Enviroment = new X_E_Enviroment(Env.getCtx()	, client.get_ValueAsInt(X_E_Enviroment.COLUMNNAME_E_Enviroment_ID), null);
		return e_Enviroment;
	}
	
	public X_E_PlantType  client_getE_PlantType(MClient client) {
		X_E_PlantType e_PlantType = new X_E_PlantType(Env.getCtx()	, client.get_ValueAsInt(X_E_PlantType.COLUMNNAME_E_PlantType_ID), null);
		return e_PlantType;
	}
	
	public String  client_getEmail(MClient client) {
		String email = 	client.get_ValueAsString("EMail");
		return email;
	}
	
	public X_E_Activity  client_getE_Activity(MClient client) {
		X_E_Activity e_Activity = new X_E_Activity(Env.getCtx()	, client.get_ValueAsInt(X_E_Activity.COLUMNNAME_E_Activity_ID), null);
		return e_Activity;
	}
	
	public X_E_DocType  docType_getE_DocType(MDocType docType) {
		X_E_DocType e_docType = new X_E_DocType(Env.getCtx()	, docType.get_ValueAsInt(X_E_DocType.COLUMNNAME_E_DocType_ID), null);
		return e_docType;
	}
	
	public String  invoice_ei_codigoGeneracion(MInvoice invoice) {
		String ei_codigoGeneracion = invoice.get_ValueAsString("ei_codigoGeneracion");
		return ei_codigoGeneracion;
	}
	
	public String  invoice_ei_selloRecibido(MInvoice invoice) {
		String ei_selloRecibido = invoice.get_ValueAsString("ei_selloRecibido");
		return ei_selloRecibido;
	}	

	
	public String  invoice_ei_numeroControl(MInvoice invoice) {
		String ei_numeroControl = invoice.get_ValueAsString("ei_numeroControl");
		return ei_numeroControl;
	}
	
	public X_E_Recipient_Identification  bPartner_getE_Recipient_Identification(MBPartner partner) {
		X_E_Recipient_Identification e_Recipient_Identification = new X_E_Recipient_Identification(Env.getCtx()	, partner.get_ValueAsInt(X_E_Recipient_Identification.COLUMNNAME_E_Recipient_Identification_ID), null);
		return e_Recipient_Identification;
	}
	
	public String  bPartner_getPhone(MBPartner partner) {
		String phone = partner.get_ValueAsString("Phone");
		return phone;
	}
	
	public String  bPartner_getEmail(MBPartner partner) {
		String email = partner.get_ValueAsString("Email");
		return email;
	}
	
	public X_E_Activity  bPartner_getE_Activity(MBPartner partner) {
		X_E_Activity e_Activity = new X_E_Activity(Env.getCtx()	, partner.get_ValueAsInt(X_E_Activity.COLUMNNAME_E_Activity_ID), null);
		return e_Activity;
	}
	
	public X_E_BPType  bPartner_getE_BPType(MBPartner partner) {
		X_E_BPType e_BPType = new X_E_BPType(Env.getCtx()	, partner.get_ValueAsInt(X_E_BPType.COLUMNNAME_E_BPType_ID), null);
		return e_BPType;
	}
	
	public String  city_getRegionValue(MCity city) {
		X_C_Region region = new X_C_Region(Env.getCtx()	, city.get_ValueAsInt(X_C_Region.COLUMNNAME_C_Region_ID), null);
		String value = region.get_ValueAsString("Value");
		return value;
	}
	
	public String  city_getValue(MCity city) {		
		String value = city.get_ValueAsString("Value");
		return value;
	}
	
	public String  country_getValue(MCountry country) {		
		String value = country.get_ValueAsString("Value");
		return value;
	}
	
	public X_E_Duties  tax_getE_Duties(MTax tax) {
		X_E_Duties e_Duties = new X_E_Duties(Env.getCtx()	, tax.get_ValueAsInt(X_E_Duties.COLUMNNAME_E_Duties_ID), null);
		return e_Duties;
	}
	public X_E_TimeSpan  paymentterm_getE_TimeSpan(MPaymentTerm paymentTerm) {
		X_E_TimeSpan e_TimeSpan = new X_E_TimeSpan(Env.getCtx()	, paymentTerm.get_ValueAsInt(X_E_TimeSpan.COLUMNNAME_E_TimeSpan_ID), null);
		return e_TimeSpan;
	}
	
	
	

	public MInvoice invoiceLine_getRef_InvoiceLine_getC_Invoice(MInvoiceLine invoiceLine){
		MInvoiceLine refInvoiceLine = new MInvoiceLine(Env.getCtx(), invoiceLine.get_ValueAsInt("Ref_InvoiceLine_ID"), null);
		MInvoice invoice = (MInvoice)refInvoiceLine.getC_Invoice();
		return invoice;
	}
	
	
	
	

}
