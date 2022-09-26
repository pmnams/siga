package br.gov.jfrj.itextpdf;

import br.gov.jfrj.itextpdf.LocalizaAnotacao.LocalizaAnotacaoResultado;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.SigaHTTP;
import br.gov.jfrj.siga.base.SigaMessages;
import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stamp {
    private static final String VALIDAR_ASSINATURA_URL = "/sigaex/app/validar-assinatura?pessoa=";
    private static float QRCODE_LEFT_MARGIN_IN_CM = 3.0F;
    private static float BARCODE_HEIGHT_IN_CM = 2.0F;
    private static final int TEXT_TO_CIRCLE_INTERSPACE = 2;
    private static final float TEXT_HEIGHT = 5F;
    private static final float SAFETY_MARGIN = 0.1F;
    private static final float CM_UNIT = 72.0f / 2.54F;
    private static float PAGE_BORDER_IN_CM = 0.8F;
    private static float STAMP_BORDER_IN_CM = 0.2F;

    static {
        if (SigaMessages.isSigaSP()) { // Adequa marcas para SP
            QRCODE_LEFT_MARGIN_IN_CM = 0.6f;
            BARCODE_HEIGHT_IN_CM = 2.0f;
            PAGE_BORDER_IN_CM = 0.5f;
            STAMP_BORDER_IN_CM = 0.2f;
        }

    }

    public static byte[] stamp(byte[] abPdf, String sigla, boolean rascunho, boolean copia, boolean cancelado,
                               boolean semEfeito, boolean internoProduzido, boolean isPdf, String qrCode, String mensagem, Integer paginaInicial,
                               Integer paginaFinal, Integer cOmitirNumeracao, String instancia, String orgaoUsu, String marcaDaguaDoModelo,
                               List<Long> idsAssinantes) throws DocumentException, IOException {

        if (idsAssinantes != null && idsAssinantes.size() > 0 && Boolean.TRUE.equals(Prop.getBool("assinatura.estampar")))
            abPdf = estamparAssinaturas(abPdf, idsAssinantes);

        PdfReader pdfIn = new PdfReader(abPdf);
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);

        if (pdfIn.getAcroFields().getSignatureNames().isEmpty())
            try (ByteArrayOutputStream boA4 = new ByteArrayOutputStream()) {
			/*-- Alterado de PdfWriter p/ PdfCopy(Essa classe permite manter os "stamps" originais do arquivo importado) 
			por Marcos(CMSP) em 21/02/19 --*/
                // PdfCopy writer = new PdfCopy(doc, boA4);
                /*-- Alerado de volta pois ficou desabilitado o redimensionamento do PDF de modo
                 *   que os códigos de barra 2D e 3D não ficassem por cima do texto. Por Renato em 25/04/2019 --*/
                PdfWriter writer = PdfWriter.getInstance(doc, boA4);
                doc.open();
                PdfContentByte cb = writer.getDirectContent();

                // Resize every page to A4 size
                //
                // double thetaRotation = 0.0;
                for (int i = 1; i <= pdfIn.getNumberOfPages(); i++) {
                    int rot = pdfIn.getPageRotation(i);

                    PdfImportedPage page = writer.getImportedPage(pdfIn, i);
                    float w = page.getWidth();
                    float h = page.getHeight();

                    doc.setPageSize((rot != 0 && rot != 180) ^ (w > h) ? PageSize.A4.rotate() : PageSize.A4);
                    doc.newPage();

                    cb.saveState();

                    if (rot != 0 && rot != 180) {
                        float swap = w;
                        w = h;
                        h = swap;
                    }

                    float pw = doc.getPageSize().getWidth();
                    float ph = doc.getPageSize().getHeight();
                    double scale = Math.min(pw / w, ph / h);

                    // do my transformations :
                    cb.transform(AffineTransform.getScaleInstance(scale, scale));

                    if (!internoProduzido && !isPdf) {
                        cb.transform(AffineTransform.getTranslateInstance(pw * SAFETY_MARGIN, ph * SAFETY_MARGIN));
                        cb.transform(AffineTransform.getScaleInstance(1.0f - 2 * SAFETY_MARGIN, 1.0f - 2 * SAFETY_MARGIN));
                    }

                    if (rot != 0) {
                        double theta = -rot * (Math.PI / 180);
                        if (rot == 180) {
                            cb.transform(AffineTransform.getRotateInstance(theta, w / 2, h / 2));
                        } else {
                            cb.transform(AffineTransform.getRotateInstance(theta, h / 2, w / 2));
                        }
                        if (rot == 90) {
                            cb.transform(AffineTransform.getTranslateInstance((w - h) / 2, (w - h) / 2));
                        } else if (rot == 270) {
                            cb.transform(AffineTransform.getTranslateInstance((h - w) / 2, (h - w) / 2));
                        }
                    }

                    // Logger.getRootLogger().error(
                    // "----- dimensoes: " + rot + ", " + w + ", " + h);
                    // Logger.getRootLogger().error("----- page: " + pw + ", " + ph);

                    // cb.transform(AffineTransform.getTranslateInstance(
                    // ((pw / scale) - w) / 2, ((ph / scale) - h) / 2));

                    // put the page
                    cb.addTemplate(page, 0, 0);
                    cb.restoreState();
                }
                doc.close();

                abPdf = boA4.toByteArray();
            }

        try (ByteArrayOutputStream bo2 = new ByteArrayOutputStream()) {
            final PdfReader reader = new PdfReader(abPdf);

            final int n = reader.getNumberOfPages();
            final PdfStamper stamp = new PdfStamper(reader, bo2);

            // adding content to each page
            int i = 0;
            PdfContentByte over;
            final BaseFont helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

            byte[] maskr = {(byte) 0xff};
            Image mask = Image.getInstance(1, 1, 1, 1, maskr);
            mask.makeMask();
            mask.setInverted(true);

            while (i < n) {
                i++;
                // watermark under the existing page
                over = stamp.getOverContent(i);

                final Barcode39 code39 = new Barcode39();
                // code39.setCode(doc.getCodigo());
                code39.setCode(sigla.replace("-", "").replace("/", "").replace(".", ""));
                code39.setStartStopText(false);
                final Image image39 = code39.createImageWithBarcode(over, null, null);
                Rectangle r = stamp.getReader().getPageSizeWithRotation(i);

                image39.setInitialRotation((float) Math.PI / 2.0f);

                image39.setAbsolutePosition(
                        r.getWidth() - image39.getHeight() + (STAMP_BORDER_IN_CM - PAGE_BORDER_IN_CM) * CM_UNIT,
                        BARCODE_HEIGHT_IN_CM * CM_UNIT
                );

                image39.setBackgroundColor(BaseColor.GREEN);
                image39.setBorderColor(BaseColor.RED);
                image39.setBorderWidth(0.5f * CM_UNIT);

                image39.setImageMask(mask);

                over.setRGBColorFill(255, 255, 255);
                mask.setAbsolutePosition(
                        r.getWidth() - image39.getHeight() - (PAGE_BORDER_IN_CM) * CM_UNIT,
                        (BARCODE_HEIGHT_IN_CM - STAMP_BORDER_IN_CM) * CM_UNIT
                );

                mask.scaleAbsolute(
                        image39.getHeight() + 2 * STAMP_BORDER_IN_CM * CM_UNIT,
                        image39.getWidth() + 2 * STAMP_BORDER_IN_CM * CM_UNIT

                );
                over.addImage(mask);

                over.setRGBColorFill(0, 0, 0);
                over.addImage(image39);

                // Estampa o logo do Siga-Doc. Atenção, pedimos que esse logo seja preservado em
                // todos os órgãos que utilizarem o Siga-Doc. Não se trata aqui da marca do
                // TRF2,
                // mas sim da identificação do sistema Siga-Doc. É importante para a
                // continuidade
                // do projeto que se faça essa divulgação.

                InputStream stream = Documento.class.getClassLoader()
                        .getResourceAsStream("/br/gov/jfrj/itextpdf/logo-siga-novo-166px.png");
                if (stream != null) {
                    byte[] ab = IOUtils.toByteArray(stream);
                    final Image logo = Image.getInstance(ab);
//				
                    logo.scaleToFit(image39.getHeight(), image39.getHeight());

                    logo.setAbsolutePosition(
                            r.getWidth() - image39.getHeight() + (STAMP_BORDER_IN_CM - PAGE_BORDER_IN_CM) * CM_UNIT,
                            PAGE_BORDER_IN_CM * CM_UNIT
                    );

                    logo.setBackgroundColor(BaseColor.GREEN);
                    logo.setBorderColor(BaseColor.RED);
                    logo.setBorderWidth(0.5f * CM_UNIT);
                    logo.setImageMask(mask);

                    over.setRGBColorFill(255, 255, 255);
                    mask.setAbsolutePosition(
                            r.getWidth() - image39.getHeight() - (PAGE_BORDER_IN_CM) * CM_UNIT,
                            (PAGE_BORDER_IN_CM - STAMP_BORDER_IN_CM) * CM_UNIT
                    );
                    mask.scaleAbsolute(
                            image39.getHeight() + 2 * STAMP_BORDER_IN_CM * CM_UNIT,
                            image39.getHeight() * logo.getHeight() / logo.getWidth() + 2 * STAMP_BORDER_IN_CM * CM_UNIT
                    );
                    over.addImage(mask);

                    over.setRGBColorFill(255, 255, 255);
                    logo.setAnnotation(new Annotation(0, 0, 0, 0, "https://linksiga.trf2.jus.br"));

                    if (Prop.isGovSP()) {
                        if (i == 1)
                            over.addImage(logo);
                    } else {
                        over.addImage(logo);
                    }
                }

                float QRCODE_SIZE_IN_CM = 1.5f;
                if (qrCode != null) {
                    java.awt.Image imgQRCode = createQRCodeImage(qrCode);
                    Image imageQRCode = Image.getInstance(imgQRCode, Color.BLACK, true);
                    imageQRCode.scaleAbsolute(QRCODE_SIZE_IN_CM * CM_UNIT, QRCODE_SIZE_IN_CM * CM_UNIT);
                    imageQRCode.setAbsolutePosition(QRCODE_LEFT_MARGIN_IN_CM * CM_UNIT, PAGE_BORDER_IN_CM * CM_UNIT);

                    over.setRGBColorFill(255, 255, 255);
                    mask.setAbsolutePosition((QRCODE_LEFT_MARGIN_IN_CM - STAMP_BORDER_IN_CM) * CM_UNIT,
                            (PAGE_BORDER_IN_CM - STAMP_BORDER_IN_CM) * CM_UNIT);
                    mask.scaleAbsolute((QRCODE_SIZE_IN_CM + 2 * STAMP_BORDER_IN_CM) * CM_UNIT,
                            (QRCODE_SIZE_IN_CM + 2 * STAMP_BORDER_IN_CM) * CM_UNIT);
                    over.addImage(mask);

                    over.setRGBColorFill(0, 0, 0);
                    over.addImage(imageQRCode);
                }

                if (mensagem != null) {
                    PdfPTable table = new PdfPTable(1);
                    table.setTotalWidth(r.getWidth() - image39.getHeight() - (QRCODE_LEFT_MARGIN_IN_CM
                            + QRCODE_SIZE_IN_CM + 4 * STAMP_BORDER_IN_CM + PAGE_BORDER_IN_CM) * CM_UNIT);
                    PdfPCell cell = new PdfPCell(
                            new Paragraph(
                                    mensagem,
                                    FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK)
                            )
                    );
                    cell.setBorderWidth(0);
                    table.addCell(cell);

                    over.setRGBColorFill(255, 255, 255);
                    mask.setAbsolutePosition(
                            (QRCODE_LEFT_MARGIN_IN_CM + QRCODE_SIZE_IN_CM + STAMP_BORDER_IN_CM) * CM_UNIT,
                            (PAGE_BORDER_IN_CM - STAMP_BORDER_IN_CM) * CM_UNIT);
                    mask.scaleAbsolute(2 * STAMP_BORDER_IN_CM * CM_UNIT + table.getTotalWidth(),
                            2 * STAMP_BORDER_IN_CM * CM_UNIT + table.getTotalHeight());
                    over.addImage(mask);

                    over.setRGBColorFill(0, 0, 0);
                    table.writeSelectedRows(0, -1,
                            (QRCODE_LEFT_MARGIN_IN_CM + QRCODE_SIZE_IN_CM + 2 * STAMP_BORDER_IN_CM) * CM_UNIT,
                            table.getTotalHeight() + PAGE_BORDER_IN_CM * CM_UNIT, over);
                }

                if (cancelado) {
                    tarjar(SigaMessages.getMessage("marcador.cancelado.label").toUpperCase(), over, helv, r);
                } else if (rascunho && copia) {
                    tarjar("CÓPIA DE MINUTA", over, helv, r);
                } else if (rascunho) {
                    tarjar("MINUTA", over, helv, r);
                } else if (semEfeito) {
                    tarjar(SigaMessages.getMessage("marcador.semEfeito.label").toUpperCase(), over, helv, r);
                } else if (copia) {
                    tarjar("CÓPIA", over, helv, r);
                } else if (SigaMessages.isSigaSP() && ("treinamento".equals(Prop.get("/siga.ambiente")))) {
                    tarjar("CAPACITAÇÃO", over, helv, r);
                } else if (SigaMessages.isSigaSP() && ("homolog".equals(Prop.get("/siga.ambiente")))) {
                    tarjar("HOMOLOGAÇÃO", over, helv, r);
                } else if (!marcaDaguaDoModelo.isEmpty()) {
                    tarjar(marcaDaguaDoModelo, over, helv, r);
                } else if (!"prod".equals(Prop.get("/siga.ambiente"))) {
                    tarjar("INVÁLIDO", over, helv, r);
                }

                // Imprime um círculo com o número da página dentro.

                if (paginaInicial != null) {
                    String sFl = String.valueOf(paginaInicial + i - 1);
                    // Se for a ultima pagina e o numero nao casar, acrescenta "-" e
                    // pagina final
                    if (n == i) {
                        if (paginaFinal != paginaInicial + n - 1) {
                            sFl = sFl + "-" + paginaFinal;
                        }
                    }
                    if (i > cOmitirNumeracao) {
                        // tamanho fonte, número
                        int textHeight = 23;
                        // Raio do circulo interno
                        float radius = 18f;

                        if (SigaMessages.isSigaSP()) {
                            // tamanho fonte, número
                            textHeight = 12;
                            // Raio do circulo interno
                            radius = 12f;
                            // não exibe órgão
                            orgaoUsu = "";
                        }

                        // Distancia entre o circulo interno e o externo
                        final float circleInterspace = Math.max(helv.getAscentPoint(instancia, TEXT_HEIGHT),
                                helv.getAscentPoint(orgaoUsu, TEXT_HEIGHT))
                                - Math.min(helv.getDescentPoint(instancia, TEXT_HEIGHT),
                                helv.getDescentPoint(orgaoUsu, TEXT_HEIGHT))
                                + 2 * TEXT_TO_CIRCLE_INTERSPACE;

                        // Centro do circulo
                        float xCenter = r.getWidth() - 1.8f * (radius + circleInterspace);
                        float yCenter = r.getHeight() - 1.8f * (radius + circleInterspace);

                        over.saveState();
                        final PdfGState gs = new PdfGState();
                        gs.setFillOpacity(1f);
                        over.setGState(gs);
                        over.setColorFill(BaseColor.BLACK);

                        over.saveState();
                        over.setColorStroke(BaseColor.BLACK);
                        over.setLineWidth(1f);
                        over.setColorFill(BaseColor.WHITE);

                        // Circulo externo
                        over.circle(xCenter, yCenter, radius + circleInterspace);
                        over.fill();
                        over.circle(xCenter, yCenter, radius + circleInterspace);
                        over.stroke();

                        // Circulo interno
                        over.circle(xCenter, yCenter, radius);
                        over.stroke();
                        over.restoreState();

                        {
                            over.saveState();
                            over.beginText();
                            over.setFontAndSize(helv, TEXT_HEIGHT);

                            // Escreve o texto superior do carimbo
                            float fDescent = helv.getDescentPoint(instancia, TEXT_HEIGHT);
                            showTextOnArc(over, instancia, helv, xCenter, yCenter,
                                    radius - fDescent + TEXT_TO_CIRCLE_INTERSPACE, true);

                            // Escreve o texto inferior
                            float fAscent = helv.getAscentPoint(orgaoUsu, TEXT_HEIGHT);
                            showTextOnArc(over, orgaoUsu, helv, xCenter, yCenter,
                                    radius + fAscent + TEXT_TO_CIRCLE_INTERSPACE, false);
                            over.endText();
                            over.restoreState();
                        }

                        over.beginText();

                        // Diminui o tamanho do font ate que o texto caiba dentro do
                        // circulo interno
                        while (helv.getWidthPoint(sFl, textHeight) > (2 * (radius - TEXT_TO_CIRCLE_INTERSPACE)))
                            textHeight--;
                        float fAscent = helv.getAscentPoint(sFl, textHeight) + helv.getDescentPoint(sFl, textHeight);
                        over.setFontAndSize(helv, textHeight);
                        over.showTextAligned(Element.ALIGN_CENTER, sFl, xCenter, yCenter - 0.5f * fAscent, 0);
                        over.endText();
                        over.restoreState();
                    }
                }

            }
            stamp.close();
            return bo2.toByteArray();
        } catch (WriterException e) {
            e.printStackTrace();
            /// TODO: 23/01/2022 qrcode exception
            return null;
        }
    }

    private static byte[] estamparAssinaturas(byte[] pdf, List<Long> idsAssinantes) {
        try {
            PDDocument doc;
            doc = PDDocument.load(pdf);

            List<String> seek = new ArrayList<>();
            for (Long id : idsAssinantes)
                seek.add(VALIDAR_ASSINATURA_URL + id + "&");

            List<LocalizaAnotacaoResultado> l = LocalizaAnotacao.localizar(doc, seek);
            if (l == null)
                return pdf;

            byte[] abStamp = SigaHTTP
                    .convertStreamToByteArray(Stamp.class.getResourceAsStream("assinado-digitalmente.png"), 4096);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, abStamp, "assinado digitalmente");

            Set<String> set = new HashSet<>();

            for (LocalizaAnotacaoResultado i : l) {
                System.out.println("achei: " + i.page + ", (" + i.lowerLeftX + ", " + i.upperRightY + ")");
                if (set.contains(i.uri))
                    continue;
                set.add(i.uri);
                System.out.println("processando: " + i.page + ", (" + i.lowerLeftX + ", " + i.upperRightY + ")");

                PDPage page = doc.getPage(i.page - 1);

                PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
                float height = i.height * 1.2f;
                float width = pdImage.getWidth() * (height / pdImage.getHeight());
                float lowerLeftX = (i.lowerLeftX + i.width / 2) - width / 2;
                float upperRightY = i.upperRightY;
                contents.drawImage(pdImage, lowerLeftX, upperRightY, width, height);
                System.out.println("Image inserted");
                contents.close();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            doc.close();
            baos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static java.awt.Image createQRCodeImage(String url) throws WriterException {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(url, BarcodeFormat.QR_CODE, 500, 500,
                        ImmutableMap.of(com.google.zxing.EncodeHintType.MARGIN, 0));

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private static void tarjar(String tarja, PdfContentByte over, final BaseFont helv, Rectangle r) {
        over.saveState();
        final PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.5f);
        over.setGState(gs);
        over.setColorFill(BaseColor.GRAY);
        over.beginText();
        over.setFontAndSize(helv, 72);
        over.showTextAligned(Element.ALIGN_CENTER, tarja, r.getWidth() / 2, r.getHeight() / 2, 45);
        over.endText();
        over.restoreState();
    }

    // Desenha texto ao redor de um circulo, acima ou abaixo
    //
    private static void showTextOnArc(PdfContentByte cb, String text, BaseFont font, float xCenter,
                                      float yCenter, float radius, boolean top) {
        float fTotal = 0;
        float[] aPos = new float[text.length()];
        for (int i = 0; i < text.length(); i++) {
            float f = font.getWidthPoint(text.substring(i, i + 1), TEXT_HEIGHT);
            aPos[i] = f / 2 + fTotal;
            fTotal += f;
        }
        float fAscent = font.getAscentPoint(text, TEXT_HEIGHT);

        for (int i = 0; i < text.length(); i++) {
            double theta;

            if (top)
                theta = (aPos[i] - fTotal / 2) / radius;
            else
                theta = (-1 * (aPos[i] - fTotal / 2) / (radius - fAscent) + Math.PI);

            cb.showTextAligned(
                    Element.ALIGN_CENTER,
                    text.substring(i, i + 1),
                    (float) (xCenter + radius * Math.sin(theta)),
                    (float) (yCenter + radius * Math.cos(theta)),
                    (float) ((-theta + (top ? 0 : Math.PI)) * 180 / Math.PI)
            );
        }
    }

}
