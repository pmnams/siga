/*-*****************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 *
 *     This file is part of SIGA.
 *
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.ex.util;

import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Prop;
import br.gov.jfrj.siga.base.SigaFormats;
import br.gov.jfrj.siga.cp.CpModelo;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.hibernate.ExDao;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.jsoup.parser.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessadorModeloFreemarker implements ProcessadorModelo,
        TemplateLoader {

    private final Configuration cfg;

    public ProcessadorModeloFreemarker() {
        super();
        cfg = new Configuration(Configuration.VERSION_2_3_30);
        // Specify the data source where the template files come from.
        cfg.setTemplateLoader(this);
        // Specify how templates will see the data-model.
        //cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setWhitespaceStripping(true);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setNumberFormat("0.######");
        cfg.setLocalizedLookup(false);
        cfg.setLogTemplateExceptions(false);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String processarModelo(CpOrgaoUsuario ou, Map<String, Object> attrs,
                                  Map<String, Object> params) {
        // Create the root hash
        Map<String, Object> root = new HashMap<>();
        root.put("func", new FuncoesEL());
        root.put("fmt", new SigaFormats());
        root.put("exbl", Ex.getInstance().getBL());

        root.putAll(attrs);
        root.put("param", params);

        if (attrs.containsKey("entrevista"))
            root.put("gerar_entrevista", true);
        if (attrs.containsKey("processar_modelo"))
            root.put("gerar_documento", true);
        if (attrs.containsKey("formulario"))
            root.put("gerar_formulario", true);
        if (attrs.containsKey("resumo"))
            root.put("gerar_resumo", true);
        if (attrs.containsKey("finalizacao"))
            root.put("gerar_finalizacao", true);
        if (attrs.containsKey("gravacao"))
            root.put("gerar_gravacao", true);
        if (attrs.containsKey("assinatura"))
            root.put("gerar_assinatura", true);
        if (attrs.containsKey("pre_assinatura"))
            root.put("gerar_pre_assinatura", true);
        if (attrs.containsKey("partes"))
            root.put("gerar_partes", true);
        if (attrs.containsKey("descricao"))
            root.put("gerar_descricao", true);
        if (attrs.containsKey("descricaodefault"))
            root.put("gerar_descricaodefault", true);

        StringBuilder templateCode = new StringBuilder("[#compress]\n[#include \"DEFAULT\"][#include \"GERAL\"]");

        if (ou != null)
            templateCode.append("\n[#include \"")
                    .append(ou.getAcronimoOrgaoUsu())
                    .append("\"]");

        if (ou != null && ou.getBrasao() != null)
            templateCode.append("\n[#assign _pathBrasao = \"/siga/public/app/orgaoUsuario/")
                    .append(ou.getId())
                    .append("/brasao\" /]");

        if (attrs.get("template") != null)
            templateCode.append("\n")
                    .append(attrs.get("template"));

        templateCode.append("\n[/#compress]");

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Writer out = new OutputStreamWriter(baos, StandardCharsets.UTF_8)
        ) {
            Template temp = new Template(
                    (String) attrs.get("nmMod"),
                    templateCode.toString(),
                    cfg
            );

            temp.process(root, out);
            out.flush();
            String processed = baos.toString(StandardCharsets.UTF_8.name());

            // todo. Verificar a necessidade do reprocessamento
            // Reprocessar para substituir variáveis declaradas nos campos da entrevista
            if (root.get("gerar_documento") != null || root.get("gerar_descricao") != null) {
                baos.reset();

                // Altera para desfazer o HTML encoding de dentro das operações do Freemarker
                Pattern p = Pattern.compile("\\$\\{([^\\}]+)\\}");
                Matcher m = p.matcher(processed);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String group = m.group(1);
                    String unescapeEntities = "\\${" + Parser.unescapeEntities(group, true) + "}";
                    m.appendReplacement(sb, unescapeEntities);
                }
                m.appendTail(sb);
                processed = sb.toString();

                // Altera para que não seja necessário aplicar o (...)! manualmente
                processed = processed.replaceAll("\\$\\{([^\\}]+)\\}", "\\${($1)!}");

                temp = new Template(((String) attrs.get("nmMod")) + " (post-processing)", new StringReader(processed), cfg);
                temp.process(root, out);
                out.flush();
                processed = baos.toString(StandardCharsets.UTF_8.name());
            }

            return processed;
        } catch (TemplateException e) {
            if (e.getCause() != null && e.getCause() instanceof AplicacaoException)
                throw (AplicacaoException) e.getCause();

            throw new RuntimeException("Erro executanto template Freemarker: " + e.getMessage(), e);
        } catch (IOException e) {
            return "Erro executando template FreeMarker\n\n" + e.getMessage();
        }
    }

    public void closeTemplateSource(Object arg0) throws IOException {
    }

    static LoadingCache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(Prop.get("debug.default.template.pathname") == null ? 5 : 1,
                    Prop.get("debug.default.template.pathname") == null ? TimeUnit.MINUTES : TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                public String load(String source) throws Exception {
                    CpModelo mod;
                    if ("DEFAULT".equals(source)) {
                        return FreemarkerDefault.getDefaultTemplate();
                    } else if ("GERAL".equals(source)) {
                        mod = ExDao.getInstance().consultaCpModeloGeral();
                    } else {
                        mod = ExDao.getInstance().consultaCpModeloPorNome(source);
                    }

                    String conteudoBlob = "";
                    if (mod != null)
                        conteudoBlob = mod.getConteudoBlobString() == null ? "" : mod.getConteudoBlobString();
                    return conteudoBlob;
                }
            });

    public Object findTemplateSource(String source) throws IOException {
        try {
            return cache.get(source);
        } catch (ExecutionException e) {
            throw new IOException("Não foi possível obter o template: " + source, e);
        }
    }

    public long getLastModified(Object arg0) {
        return 0;
    }

    public Reader getReader(Object arg0, String arg1) throws IOException {
        return new StringReader((String) arg0);
    }

}
