/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.shw.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.validation.constraints.Null;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MStorage;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Language;

/**
 *	Generate Invoices
 *	
 *  @author SHW
 *  @version $Id: SHW_InvoiceGenerate.java,v 1.2 2015/01/24 00:51:01 mc Exp $
 */
public class SHW_InvoiceGeneratePurchase extends SvrProcess
{
	/**	Date Invoiced			*/
	private Timestamp	p_DateInvoiced = null;
	/** Consolidate				*/
	private boolean		p_ConsolidateDocument = true;
	/** Invoice Document Action	*/
	private String		p_docAction = "";
	
	/**	The current Invoice	*/
	private MInvoice 	newinvoice = null;
	/**	The current Shipment	*/
	private MInOut	 	m_ship = null;
	/** Number of Invoices		*/
	private int			m_created = 0;
	/**	Line Number				*/
	private int			m_line = 0;
	/**	Business Partner		*/
	private MBPartner	m_bp = null;
	private int 		p_C_Invoice_ID = 0;
	protected List<MOrder> m_records = null;
	protected List<MInvoice> m_invoices = null;
	private String error = "";
	private int errorcount = 0;
	private int successcount = 0;
	private int ordercount = 0;
	private int p_C_Doctype_ID = 0;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("ConsolidateDocument"))
				p_ConsolidateDocument = para.getParameterAsBoolean();
			else if (name.equals("DocAction"))
				p_docAction = para.getParameterAsString();
			else if (name.equals(MInvoice.COLUMNNAME_C_Invoice_ID))
				p_C_Invoice_ID = para.getParameterAsInt();
			else if (name.equals(MInvoice.COLUMNNAME_DateInvoiced))
				p_DateInvoiced = para.getParameterAsTimestamp();
			else if (name.equals(MDocType.COLUMNNAME_C_DocType_ID))
				p_C_Doctype_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

		//	Login Date
		if (p_DateInvoiced == null)
			p_DateInvoiced = Env.getContextAsDate(getCtx(), "#Date");
		if (p_DateInvoiced == null)
			p_DateInvoiced = new Timestamp(System.currentTimeMillis());
	}	//	prepare

	/**
	 * 	Generate Invoices
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		String whereClause = "";
		if (getRecord_ID() != 0)
		{
			MOrder order  = new MOrder(getCtx(), getRecord_ID(), get_TrxName());
			m_records = new ArrayList<MOrder>();
			m_records.add(order);
		}
		else
		{		
			StringBuffer orderClause = new StringBuffer();
			if (!p_ConsolidateDocument)
				orderClause.append("C_BPartner_ID, C_Project_ID, c_doctypetarget_ID");
			else 
				orderClause.append("C_BPartner_ID");
			whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " +
					" AND T_Selection.T_Selection_ID=c_order.C_Order_ID)";
			m_records = new Query(getCtx(), MOrder.Table_Name, whereClause, get_TrxName())
					.setParameters(getAD_PInstance_ID())
					.setOrderBy(orderClause.toString())
					.setClient_ID()
					.list();
		}
		ordercount = m_records.size();
		m_invoices = new ArrayList<MInvoice>();
		for (MOrder order:m_records)
		{			
			try
			{
				generate(order);		
			}	
			catch (Exception e)
			{
				errorcount = errorcount + 1;
				error = error + "," + order.getDocumentNo();
				continue;
			}
		}

		completeInvoice();

		String result = "Fact. No";
		for (MInvoice inv:m_invoices)
		{
			Env.setContext(getCtx(), "@WhereClause@", whereClause);
			result = result + ", " + inv.getDocumentInfo();
 		}

		
		result = result + " Ordenes proc.: " + ordercount + " errores:" + errorcount ;
		if (error.length()!=0)
			result = result + " " + error;
		
		return result;
	}	//	doIt
	
	
	/**
	 * 	Generate Shipments
	 * 	@param pstmt order query 
	 *	@return info
	 */
	private String generate (MOrder order) throws Exception
	{

		if (!p_ConsolidateDocument 
				|| (newinvoice != null 
				&& newinvoice.getC_BPartner_ID() != order.getC_BPartner_ID()) )
			completeInvoice();

		//	After Delivery
		MDocType dt = (MDocType)order.getC_DocTypeTarget();

		createShipment(order, dt, null);
		MInOut[] shipments = order.getShipments();
		for (int i = 0; i < shipments.length; i++)
		{
			MInOut ship = shipments[i];
			if (!ship.isComplete()		//	ignore incomplete or reversals 
					|| ship.getDocStatus().equals(MInOut.DOCSTATUS_Reversed))
				continue;
			MInOutLine[] shipLines = ship.getLines(true);
			for (int j = 0; j < shipLines.length; j++)
			{
				MInOutLine shipLine = shipLines[j];
				Boolean isInvoiced = shipLine.isInvoiced();
				if (shipLine.getC_OrderLine_ID()!= 0)
				{
					if (shipLine.getC_OrderLine().getQtyInvoiced()
							.compareTo(shipLine.getC_OrderLine().getQtyOrdered()) != 0)
						isInvoiced = false;
				}

				if (!order.isOrderLine(shipLine.getC_OrderLine_ID()))
					continue;
				if (!isInvoiced)
					createLine (order, ship, shipLine);
			}
			m_line += 10;
		}


		return "";
	}	//	generate
	
	
	
	/**************************************************************************
	 * 	Create Invoice Line from Order Line
	 *	@param order order
	 *	@param orderLine line
	 *	@param qtyInvoiced qty
	 *	@param qtyEntered qty
	 */
	private void createLine (MOrder order, MOrderLine orderLine, 
		BigDecimal qtyInvoiced, BigDecimal qtyEntered)
	{
		if (newinvoice == null)
		{
			newinvoice = new MInvoice (order, 0, p_DateInvoiced);
			if (p_C_Doctype_ID != 0)
				newinvoice.setC_DocTypeTarget_ID(p_C_Doctype_ID);
			newinvoice.setDescription(order.getC_Project().getValue() + "/");
			
			// wenn's mehrere Rechnungen gibt, werden sie gelöscht
			newinvoice.set_ValueOfColumn("DocumentoDeTransporte", order.get_Value("DocumentoDeTransporte"));
			newinvoice.set_ValueOfColumn("CodigoDeDeclaracion", order.get_Value("CodigoDeDeclaracion"));
			newinvoice.set_ValueOfColumn("ReferenciaDeDeclaracion", order.get_Value("ReferenciaDeDeclaracion"));
			String ProviderPO = order.get_ValueAsString("ProviderPO");
			String ProviderPOField = newinvoice.get_ValueAsString("ProviderPO");
			if (!ProviderPOField.contains(ProviderPO))
				ProviderPOField = ProviderPOField + ", " + order.get_ValueAsString("ProviderPO");
			newinvoice.set_ValueOfColumn("Provider", ProviderPOField);
			newinvoice.saveEx();
			
			m_invoices.add(newinvoice);
			m_line = 0;
		}
		else
		{
			if(!newinvoice.getDescription().contains(order.getC_Project().getValue())){
				newinvoice.setDescription(newinvoice.getDescription() + order.getC_Project().getValue() + "/");
				newinvoice.saveEx();
			}				
		}
		//	
		MInvoiceLine line = new MInvoiceLine (newinvoice);
		line.setOrderLine(orderLine);
		line.setQtyInvoiced(qtyInvoiced);
		line.setQtyEntered(qtyEntered);
		m_line += 10;
		line.setLine(m_line);
		//line.setLine(m_line + orderLine.getLine());
		line.saveEx();
		log.fine(line.toString());
	}	//	createLine

	/**
	 * 	Create Invoice Line from Shipment
	 *	@param order order
	 *	@param ship shipment header
	 *	@param sLine shipment line
	 */
	private void createLine (MOrder order, MInOut ship, MInOutLine sLine)
	{
		if (newinvoice == null)
		{
			if (p_C_Invoice_ID != 0)
				newinvoice = new MInvoice(getCtx(), p_C_Invoice_ID, get_TrxName());
			else
			{
				newinvoice = new MInvoice (order, p_C_Invoice_ID, p_DateInvoiced);
				newinvoice.setDescription(order.getC_Project().getValue() + "/");
				if (order.getPaymentRule()!= "" || order.getPaymentRule() != null)
					newinvoice.setPaymentRule(order.getPaymentRule());
				if (order.getC_PaymentTerm_ID() > 0)
					newinvoice.setC_PaymentTerm_ID(order.getC_PaymentTerm_ID());
				
				if (p_C_Doctype_ID != 0)
					newinvoice.setC_DocTypeTarget_ID(p_C_Doctype_ID);
				newinvoice.set_ValueOfColumn("C_BPartnerImport_ID", order.get_Value("C_BPartnerImport_ID"));
				newinvoice.set_ValueOfColumn("taxamtdeclared", order.get_Value("taxamtdeclared"));
				newinvoice.set_ValueOfColumn("taxbaseamtdeclared", order.get_Value("taxbaseamtdeclared"));
				newinvoice.saveEx();			
				
			}
			m_invoices.add(newinvoice);
		}
		else
		{
			if(order.getC_Project_ID() != 0 && !newinvoice.getDescription().contains(order.getC_Project().getValue())){
				newinvoice.setDescription(newinvoice.getDescription() + order.getC_Project().getValue() + "/");
				newinvoice.saveEx();

			}
		}
			
		//	Create Shipment Comment Line
		if (m_ship == null 
			|| m_ship.getM_InOut_ID() != ship.getM_InOut_ID())
		{
			MDocType dt = MDocType.get(getCtx(), ship.getC_DocType_ID());
			if (m_bp == null || m_bp.getC_BPartner_ID() != ship.getC_BPartner_ID())
				m_bp = new MBPartner (getCtx(), ship.getC_BPartner_ID(), get_TrxName());
			
			//	Reference: Delivery: 12345 - 12.12.12
			MClient client = MClient.get(getCtx(), order.getAD_Client_ID ());
			String AD_Language = client.getAD_Language();
			if (client.isMultiLingualDocument() && m_bp.getAD_Language() != null)
				AD_Language = m_bp.getAD_Language();
			if (AD_Language == null)
				AD_Language = Language.getBaseAD_Language();
			java.text.SimpleDateFormat format = DisplayType.getDateFormat 
				(DisplayType.Date, Language.getLanguage(AD_Language));
			String reference = dt.getPrintName(m_bp.getAD_Language())
				+ ": " + ship.getDocumentNo() 
				+ " - " + format.format(ship.getMovementDate());
			m_ship = ship;
			/*//
			MInvoiceLine line = new MInvoiceLine (m_invoice);
			line.setIsDescription(true);
			line.setDescription(reference);
			m_line += 10;
			line.setLine(m_line);
			//line.setLine(m_line + sLine.getLine() - 2);
			if (!line.save())
				throw new IllegalStateException("Could not create Invoice Comment Line (sh)");
			//	Optional Ship Address if not Bill Address
			if (order.getBill_Location_ID() != ship.getC_BPartner_Location_ID())
			{
				MLocation addr = MLocation.getBPLocation(getCtx(), ship.getC_BPartner_Location_ID(), null);
				line = new MInvoiceLine (m_invoice);
				line.setIsDescription(true);
				line.setDescription(addr.toString());
				//line.setLine(m_line + sLine.getLine() - 1);
				if (!line.save())
					throw new IllegalStateException("Could not create Invoice Comment Line 2 (sh)");
			}*/
		}
		//	
		if (oLineExists(sLine))
			return ;
		MInvoiceLine line = new MInvoiceLine (newinvoice);
		line.setShipLine(sLine);
		if (sLine.sameOrderLineUOM())
			line.setQtyEntered(sLine.getQtyEntered());
		else
			line.setQtyEntered(sLine.getMovementQty());
		line.setQtyInvoiced(sLine.getMovementQty());
		m_line += 10;
		line.setLine(m_line);
		//line.setLine(m_line + sLine.getLine());
		//@Trifon - special handling when ShipLine.ToBeInvoiced='N'
		String toBeInvoiced = sLine.get_ValueAsString( "ToBeInvoiced" );
		if ("N".equals( toBeInvoiced )) {
			line.setPriceEntered( Env.ZERO );
			line.setPriceActual( Env.ZERO );
			line.setPriceLimit( Env.ZERO );
			line.setPriceList( Env.ZERO);
			//setC_Tax_ID(oLine.getC_Tax_ID());
			line.setLineNetAmt( Env.ZERO );
			line.setIsDescription( true );
		}
		if (!line.save())
			throw new IllegalStateException("Could not create Invoice Line (s)");
		//	Link
		sLine.setIsInvoiced(true);
		if (!sLine.save())
			throw new IllegalStateException("Could not update Shipment Line");
		
		log.fine(line.toString());
		successcount = successcount + 1;
	}	//	createLine

	
	/**
	 * 	Complete Invoice
	 */
	private void completeInvoice()
	{
		if (newinvoice != null)
		{
			if (p_docAction.equals(""))
			{
				newinvoice.saveEx();
				addLog(newinvoice.getC_Invoice_ID(), newinvoice.getDateInvoiced(), null, newinvoice.getDocumentNo());
				m_created++;
				
			}
			else if (!newinvoice.processIt(p_docAction))
			{
				log.warning("completeInvoice - failed: " + newinvoice);
				addLog("completeInvoice - failed: " + newinvoice); // Elaine 2008/11/25
			}
			newinvoice.saveEx();
			addLog(newinvoice.getC_Invoice_ID(), newinvoice.getDateInvoiced(), null, newinvoice.getDocumentNo());
			m_created++;
		}
		newinvoice = null;
		m_ship = null;
		m_line = 0;
	}	//	completeInvoice
	


	/**
	 * 	Correct Invoice
	 */
	private void correctInvoice (MInvoice inv)
	{
		boolean oneProjectInvoice=true;
		List<MInvoiceLine> ivlList = new Query(getCtx(), MInvoiceLine.Table_Name, "C_Invoice_ID=?", get_TrxName())
		.setParameters(inv.getC_Invoice_ID()).setOrderBy("C_Project_ID").list();
		int C_Project_ID = ivlList.get(0).getC_Project_ID();
		
		for(MInvoiceLine ivl: ivlList){
			if(C_Project_ID!=ivl.getC_Project_ID()){
				inv.set_ValueOfColumn("oneProjectInvoice", false);
				oneProjectInvoice=false;
				break;
			}
		}
		if(oneProjectInvoice)
			inv.setDescription("");
		else{
			inv.setC_Order_ID(0);
			inv.setC_Project_ID(0);
			//inv.set_ValueOfColumn("DocumentoDeTransporte", "");
			//inv.set_ValueOfColumn("CodigoDeDeclaracion", "");
			//inv.set_ValueOfColumn("ReferenciaDeDeclaracion", "");
			//inv.set_ValueOfColumn("ProviderPO", "");
			//inv.set_ValueOfColumn("Provider", "");
		}
		inv.saveEx();
	}
	
	private MInOut createShipment(MOrder order, MDocType dt, Timestamp movementDate)
	{
		log.info("For " + dt);
		Boolean toShip = Arrays.stream(order.getLines()).anyMatch(orderLine -> orderLine.getQtyDelivered().compareTo(orderLine.getQtyOrdered())  !=0);
		if (!toShip)
			return null;

		
		MInOut shipment = new MInOut (order, dt.getC_DocTypeShipment_ID(), movementDate);
	//	shipment.setDateAcct(getDateAcct());
		if (!shipment.save(get_TrxName()))
		{
			return null;
		}
		//
		for (MOrderLine oLine : order.getLines(true, null))
		{
			BigDecimal MovementQty = oLine.getQtyOrdered().subtract(oLine.getQtyDelivered());
			if (MovementQty.compareTo(Env.ZERO)<=0)
				continue;
			MInOutLine ioLine = new MInOutLine(shipment);
			//	Location
			int M_Locator_ID = MStorage.getM_Locator_ID (oLine.getM_Warehouse_ID(), 
					oLine.getM_Product_ID(), oLine.getM_AttributeSetInstance_ID(), 
					MovementQty, get_TrxName());
			if (M_Locator_ID == 0)		//	Get default Location
			{
				MWarehouse wh = MWarehouse.get(getCtx(), oLine.getM_Warehouse_ID());
				M_Locator_ID = wh.getDefaultLocator().getM_Locator_ID();
			}
			//
			ioLine.setOrderLine(oLine, M_Locator_ID, MovementQty);
			ioLine.setQty(MovementQty);
			if (oLine.getQtyEntered().compareTo(oLine.getQtyOrdered()) != 0)
				ioLine.setQtyEntered(MovementQty
					.multiply(oLine.getQtyEntered())
					.divide(oLine.getQtyOrdered(), 6, BigDecimal.ROUND_HALF_UP));
			if (!ioLine.save(get_TrxName()))
			{
				return null;
			}
		}
		//	Manually Process Shipment
		if (shipment.getLines(true).length==0)
		{
			shipment.deleteEx(true);
			return null;
		}
		if (!shipment.processIt(DocAction.ACTION_Complete))
			throw new AdempiereException("Error al procesar entrega " + order.getDocumentNo());
		shipment.saveEx(get_TrxName());
		if (!MOrder.DOCSTATUS_Completed.equals(shipment.getDocStatus()))
		{
			return null;
		}
		return shipment;
	}	//	createShipment
	
	private Boolean oLineExists(MInOutLine ioLine)
	{
		if (newinvoice == null)
			return false;
		for (MInvoiceLine ivl:newinvoice.getLines(true))
		{
			if (ivl.getC_OrderLine_ID() == ioLine.getC_OrderLine_ID())
				return true;
		}
		return false;
	}


	
}	//	InvoiceGenerate
