package com.solumesl.aims.saas.dou.batch.processor;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solumesl.aims.saas.adapter.util.SolumSaasUtil;
import com.solumesl.aims.saas.dou.dto.ArticleDTO;
import com.solumesl.aims.saas.dou.dto.ArticlePricing;
import com.solumesl.aims.saas.dou.dto.DouglasSaasData;
import com.solumesl.aims.saas.dou.dto.DouglasSaasDataWrapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ArticlePricingItemProcessor implements ItemProcessor<ArticlePricing, DouglasSaasDataWrapper>{
    ObjectMapper mapper = new ObjectMapper();
    @Value("${dou-properties.nfc.url:http://douglas.hu/p/####}")
    private String nfcUrl;
    private NumberFormat numberFormatGermany = NumberFormat.getNumberInstance(Locale.GERMANY);
    private NumberFormat numberFormatUS = NumberFormat.getNumberInstance(new Locale("en", "US"));
    @Override
    public DouglasSaasDataWrapper process(ArticlePricing articlePricing) throws Exception {
        DouglasSaasDataWrapper wrapper = new DouglasSaasDataWrapper();
        ArticleDTO article = articlePricing.getArticle();
        log.debug("article {}",article);
        try {
            final DouglasSaasData record = createRecord(articlePricing,  articlePricing.getArtikelNummer());
            if(record != null)
                addToList(wrapper, record);
        } catch (Exception e1) {
        }
        if(article == null || article.getEanliste() == null)
            return wrapper;
        article.getEanliste().forEach(ean->{ 
            try {
                final DouglasSaasData createRecord = createRecord(articlePricing, ean);
                if(createRecord!=null)
                    addToList(wrapper, createRecord);
            } catch (Exception e) {
            }
        });

        return wrapper;
    }

    private void addToList(DouglasSaasDataWrapper wrapper, DouglasSaasData douglasSaasData) {
        if(douglasSaasData!=null)
            wrapper.getData().add(douglasSaasData);
    }

    private DouglasSaasData createRecord(ArticlePricing articlePricing,  String uniqueId) {
        DouglasSaasData douglasSaasData = new DouglasSaasData(); 

        douglasSaasData.addRecord("STORE_ID", articlePricing.getStoreCode());
        douglasSaasData.addRecord("UniqueID", uniqueId);

        String[] euro1Arr = splitPriceCent(articlePricing.getPricing().getPreisAktuelle());
        if(SolumSaasUtil.isEmpty(euro1Arr[0])) return null;//this record shoudlnt be sent to saas
        douglasSaasData.addRecord("Euro1", decorate(euro1Arr[0]));	
        douglasSaasData.addRecord("Euro1Cent", euro1Arr[1]);

        String[] euro2Arr = splitPriceCent(articlePricing.getPricing().getPreisListe());
        douglasSaasData.addRecord("Euro2", decorate(euro2Arr[0]));	
        douglasSaasData.addRecord("Euro2Cent",euro2Arr[1]);

        douglasSaasData.addRecord("PriceCheck", determinePriceCheck(articlePricing));
        douglasSaasData.addRecord("Grundpreis", articlePricing.getPricing().getGrundpreiskennzeichen());

        String[] grundpreisArr = splitPriceCent(articlePricing.getPricing().getGrundpreis());
        douglasSaasData.addRecord("Grundpreiseuro", decorate(grundpreisArr[0]));
        douglasSaasData.addRecord("Grundpreiscent", grundpreisArr[1]);

        douglasSaasData.addRecord("Grundpreiseinheit", articlePricing.getPricing().getMengeneinheit());
        douglasSaasData.addRecord("Bezugsgroesse", getBezugsgroesseString(articlePricing));

        douglasSaasData.addRecord("Grundpreistext", getGrundpreistext(articlePricing));

        douglasSaasData.addRecord("Artikelnummer", articlePricing.getArtikelNummer());
        douglasSaasData.addRecord("Artikeltext", articlePricing.getArticle().getArtikelText());
        douglasSaasData.addRecord("Basisinhalt", articlePricing.getArticle().getBasisInhalt());

        douglasSaasData.addRecord("Preisgegenueberstellung", articlePricing.getArticle().getPreisgegenueberstellung());
        douglasSaasData.addRecord("Warengruppe",  articlePricing.getArticle().getWarengruppe());

        douglasSaasData.addRecord("Lieferantenartikelnummer", articlePricing.getArticle().getLieferantenartikelnummer());
        douglasSaasData.addRecord("Lieferantenname", articlePricing.getArticle().getLieferantenname());
        douglasSaasData.addRecord("Linientext", articlePricing.getArticle().getLinientext());

        douglasSaasData.addRecord("Depottext", articlePricing.getArticle().getDepottext());
        douglasSaasData.addRecord("Packungsinhalt", translateUnitST(SolumSaasUtil.getValidString(articlePricing.getArticle().getPackungsinhalt())));

        douglasSaasData.addRecord("Linie", articlePricing.getArticle().getLinie());
        douglasSaasData.addRecord("Depot", articlePricing.getArticle().getDepot());

        douglasSaasData.addRecord("Loeschkennzeichen", articlePricing.getArticle().getLoeschkennzeichen());
        douglasSaasData.addRecord("Layouttemplate", getLayoutTemplate(articlePricing));

        douglasSaasData.addRecord("NFC", getNfcUrl(articlePricing));
        douglasSaasData.addRecord("Linie_Artikel", getLinie_Artikel(articlePricing));
        String[] euro3Arr = splitPriceCent(articlePricing.getArticle().getUvp());
        douglasSaasData.addRecord("Euro3", decorate(euro3Arr[0]));
        douglasSaasData.addRecord("Euro3Cent", euro3Arr[1]);

        douglasSaasData.addRecord("Price5Percent", determinePrice5Percent(articlePricing));




        return douglasSaasData;
    }

    
    private String translateUnitST(String value) {
         if(value.endsWith(" ST")) {
             return value.replace(" ST", " DB");
         }
        return value;
    }

    private   String decorate(String value) {
        /**
         * 999 should look like 999 
                                99999 should look like 99 999
                                999999 should look like 999 999
                                9999999 should look like 9 999 999

         */
      if(value == null ) 
          return value;
        switch(value.length()) {
            case 4:
             return getFirstCharacter(value).concat(" ").concat(value.substring(1, 4));
            case 5:
                return value.substring(0, 2).concat(" ").concat(value.substring(2, 5));
             
            case 6:
                return value.substring(0, 3).concat(" ").concat(value.substring(3, 6));
            case 7:
                return getFirstCharacter(value).concat(" ").concat(value.substring(1, 4)).concat(" ").concat(value.substring(4, 7));
            default:
                return value; 
          }
    }

    private String getFirstCharacter(String value) {
        return String.valueOf(value.charAt(0));
    }

    private String[] splitPriceCent(String input) {

        if(input != null) {
            if( input.contains(",")){
                return input.split(",");
            }else if( input.contains(".")){
                return input.split("\\.");
            }
        } 
        List<String> list = new ArrayList<String>();list.add(input);list.add("0");
        return list.toArray(String[]::new);

    }

    private String determinePrice5Percent(ArticlePricing articlePricing) {

        String price5Percent = "0";

        try {
            String preisgegenueberstellung = articlePricing.getArticle().getPreisgegenueberstellung();

            double price1 = numberFormatGermany.parse(articlePricing.getPricing().getPreisAktuelle()).doubleValue();
            double price2 = numberFormatGermany.parse(articlePricing.getPricing().getPreisListe()).doubleValue();
            double uvp = numberFormatUS.parse(articlePricing.getArticle().getUvp()).doubleValue();
            if (uvp<=0)
            {

                if( (preisgegenueberstellung.equals("0")) && ( (price2 - 0.05 * price2) > price1) )
                {
                    price5Percent = "2";
                }
                else if( (preisgegenueberstellung.equals("1")) && ( (price2 - 0.05 * price2) > price1) )
                {
                    price5Percent = "8";
                }
            } 
            else
            {
                if( (preisgegenueberstellung.equals("0")) && ((price2 - (price2 * 0.05)) < price1) && (price2 > price1) && (uvp > price1)  )
                {		
                    price5Percent = "3";
                }
                else if( (preisgegenueberstellung.equals("0")) && ( (price2 - (price2 * 0.05)) > price1) && (price2 > price1) && (uvp > price1)  )
                {
                    price5Percent = "4";
                }
                else if( (preisgegenueberstellung.equals("0")) && ((price2 - (price2 * 0.05)) > price1) && (uvp < price1 ) )
                {
                    price5Percent = "5";
                }
                else if( (preisgegenueberstellung.equals("1")) && ((price2 - (price2 * 0.05)) < price1)  && (price2 > price1) && (uvp > price1)  )
                {
                    price5Percent = "6";
                }
                else if( (preisgegenueberstellung.equals("1")) && ((price2 - (price2 * 0.05)) > price1) && (price2 > price1) && (uvp > price1)  )
                {
                    price5Percent = "7";
                }
            }
        } catch (Exception e) {
        }
        return price5Percent;
    }

    private String getNfcUrl(ArticlePricing articlePricing) {
        return nfcUrl.replace("####", articlePricing.getArtikelNummer());
    }

    private String determinePriceCheck(ArticlePricing articlePricing) {
        String priceCheck ="E";
        try {
            if ((!articlePricing.getPricing().getPreisAktuelle().isEmpty()) && (!articlePricing.getPricing().getPreisListe().isEmpty()))
            {

                double price1 = numberFormatGermany.parse(articlePricing.getPricing().getPreisAktuelle()).doubleValue();
                double price2 = numberFormatGermany.parse(articlePricing.getPricing().getPreisListe()).doubleValue();


                if (price1 < price2)
                {
                    priceCheck = "G"; // Euro1 < Euro2
                } else if (price1 > price2)
                {
                    priceCheck = "L"; // Euro1 > Euro2
                }
            }
        } catch ( Exception e) {
        }
        return priceCheck;
    }

    private String getBezugsgroesseString(ArticlePricing articlePricing) {
        String bezugsgroesseString = StringUtils.EMPTY;
        try {
            String vergleichsinhalt = articlePricing.getPricing().getVergleichsinhalt();
            if(!vergleichsinhalt.isEmpty()) {
                Double bezugsgroesse = Double.valueOf(vergleichsinhalt);
                if (bezugsgroesse % 1.0 != 0)
                    bezugsgroesseString = String.format("%s", bezugsgroesse);
                else
                    bezugsgroesseString = String.format("%.0f", bezugsgroesse);
            }
        } catch ( Exception e) {
        }
        return bezugsgroesseString;
    }

    private String getLinie_Artikel(ArticlePricing articlePricing) {
        return "";
    }
    private String getGrundpreistext(ArticlePricing articlePricing) {
        try {
            if("X".equalsIgnoreCase(articlePricing.getPricing().getGrundpreiskennzeichen())) {
                //sql_values.add("(" + pak_array[5] + " " + BezugsgroesseString + " " + pak_array[7] + ")"); // GrundpreisText
                return SolumSaasUtil.getValidString(articlePricing.getArticle().getPackungsinhalt()) + " (" + articlePricing.getPricing().getGrundpreis() + " Ft/" + getBezugsgroesseString(articlePricing) + " " + articlePricing.getPricing().getMengeneinheit() + ")";
            }
        } catch (Exception e) {
        }
        return "";
    }
    private String getLayoutTemplate(ArticlePricing articlePricing) {
    	return  articlePricing.getPricing().getHintergrundfarbe();
    }
}
