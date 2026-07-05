import os
import xml.etree.ElementTree as ET
from xml.dom import minidom

def add_or_update_string(root, name, value):
    for elem in root.findall('string'):
        if elem.get('name') == name:
            elem.text = value
            return
    new_elem = ET.Element('string', name=name)
    new_elem.text = value
    root.append(new_elem)

def patch_file(filepath, updates):
    if not os.path.exists(filepath):
        print(f"Skipping {filepath}, does not exist.")
        return
    tree = ET.parse(filepath)
    root = tree.getroot()
    for k, v in updates.items():
        add_or_update_string(root, k, v)
    
    xmlstr = minidom.parseString(ET.tostring(root)).toprettyxml(indent="    ")
    lines = [line for line in xmlstr.split('\n') if line.strip()]
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))
    print(f"Patched {filepath}")

english_updates = {
    "risk_low_display": "Low Risk",
    "risk_medium_display": "Medium Risk",
    "risk_low_subtitle": "Safe to use",
    "risk_medium_subtitle": "Review before using",
    "goal_detail_about_count": "About count (important)",
    "goal_detail_about_count_desc": "The count is based on species number. It may not distinguish between shiny, form, costume, gender, or size variants.",
    "goal_detail_what_does_this_do": "What does this do?",
    "goal_detail_why_risk": "Why the warning?",
    "assistant_parse": "Generate",
    "search_assistant_parse": "Generate"
}

turkish_updates = {
    "risk_low_display": "Düþük Risk",
    "risk_medium_display": "Orta Risk",
    "risk_low_subtitle": "Kullanýmý güvenli",
    "risk_medium_subtitle": "Kullanmadan önce incele",
    "goal_detail_about_count": "Sayý hakkýnda (önemli)",
    "goal_detail_about_count_desc": "Sayý, tür numarasýna göre hesaplanýr. Parlak, form, kostüm veya boyut farklarýný ayýrt etmeyebilir.",
    "goal_detail_what_does_this_do": "Bu ne iþe yarar?",
    "assistant_parse": "Oluþtur",
    "search_assistant_parse": "Oluþtur",
    "goal_detail_why_risk": "Neden dikkat etmeli?",
    "risk_safe_cleanup_title": "Neden dikkat etmelisin?",
    "risk_safe_cleanup_short": "Bu arama gönderebileceðin Pokémonlarý bulur. Ancak yanlýþlýkla deðerlileri de seçebilir.",
    "risk_safe_cleanup_detailed": "Bu arama düþük özellikli ve göndermesi güvenli Pokémonlarý listeler. Fakat araya parlak, hundo veya özel etiketlediðin biri karýþabilir. Göndermeden önce listeye mutlaka göz at.",
    "risk_safe_cleanup_check1": "Sonuçlardaki her bir Pokémon'u kontrol et.",
    "risk_safe_cleanup_check2": "Parlak, hundo veya favori olanlarý sakla.",
    "risk_safe_cleanup_check3": "Emin deðilsen elinde tut.",
    "risk_candy_prep_title": "Neden dikkat etmelisin?",
    "risk_candy_prep_short": "Þeker için fazlalýklarý bulur. Ama kostüm veya forma bakmaz, sadece türe bakar.",
    "risk_candy_prep_detailed": "Çifte þeker etkinliklerinde fazlalýklarý bulmaya yarar. Sayý Pokédex numarasýna göredir, yani parlak bir Pikachu ile kostümlü bir Pikachu ayný sayýlýr. Yanlýþlýkla nadir bir formu göndermemek için hepsini kontrol et.",
    "risk_candy_prep_check1": "Sonuçlarý incele — parlak ve kostümlüler de ayný tür sayýlýr.",
    "risk_candy_prep_check2": "Özel etiketli veya favori olanlarý sakla.",
    "risk_candy_prep_check3": "Emin deðilsen elinde tut.",
    "risk_trade_fodder_title": "Neden dikkat etmelisin?",
    "risk_trade_fodder_short": "Takas edilmemiþ fazlalýklarý bulur. Ancak arkadaþlýk seviyesi ve limitleri bilemez.",
    "risk_trade_fodder_detailed": "Henüz takas etmediðin Pokémonlarý listeler. Ama takas yapabilmen diðer kiþiyle arkadaþlýk seviyene baðlýdýr. Ayrýca özel takaslar (parlak, efsanevi) günde sadece 1 kez yapýlabilir. Arama bunlarý bilemez.",
    "risk_trade_fodder_check1": "Takastan önce arkadaþlýk seviyesini kontrol et.",
    "risk_trade_fodder_check2": "Özel takaslar günde en fazla 1 kez yapýlabilir.",
    "risk_trade_fodder_check3": "Emin deðilsen elinde tut.",
    "risk_hundo_check_title": "Neden güvenli?",
    "risk_hundo_check_short": "Sadece mükemmel (15/15/15) Pokémonlarýný gösterir. Gönderme önermez.",
    "risk_hundo_check_detailed": "Sadece kusursuz özelliklere sahip Pokémonlarýný bulur. Sadece bakmak içindir — hiçbir þeyi göndermeni önermez. En iyilerini görmek için kullan.",
    "risk_hundo_check_check1": "Aç ve kusursuz Pokémonlarýnýn tadýný çýkar.",
    "risk_hundo_check_check2": "Hiçbir gönderme veya takas önermez.",
    "risk_hundo_check_check3": "Nelerin olduðunu görmek için kullan.",
    "risk_nundo_finder_title": "Neden güvenli?",
    "risk_nundo_finder_short": "Sadece tamamen sýfýr özellikli (0/0/0) Pokémonlarý gösterir. Gönderme önermez.",
    "risk_nundo_finder_detailed": "Tam olarak 0 saldýrý, 0 savunma ve 0 HP'si olan Pokémonlarý bulur. Sadece merak içindir. Hiçbir þeyi göndermeni önermez.",
    "risk_nundo_finder_check1": "Aç ve sýfýr özellikli Pokémonlarýný gör.",
    "risk_nundo_finder_check2": "Hiçbir gönderme veya takas önermez.",
    "risk_nundo_finder_check3": "Nelerin olduðunu görmek için kullan.",
    "risk_pvp_candidates_title": "Neden güvenli?",
    "risk_pvp_candidates_short": "PvP için potansiyel adaylarý bulur. Asýl güçleri tür, seviye ve özelliklerine baðlýdýr.",
    "risk_pvp_candidates_detailed": "Savaþlarda *iyi olabilecek* Pokémonlarý listeler. Ancak asýl sýrasý tam türüne, seviyesine ve IV daðýlýmýna göre deðiþir. Bu sadece bir baþlangýçtýr — her birini bir PvP hesaplayýcýsýnda kontrol etmen gerekir.",
    "risk_pvp_candidates_check1": "Adaylarý bir PvP uygulamasýnda kontrol et.",
    "risk_pvp_candidates_check2": "Hiçbir gönderme veya takas önermez.",
    "risk_pvp_candidates_check3": "Test edecek Pokémonlarý bulmak için kullan.",
    "risk_lucky_trade_title": "Neden dikkat etmelisin?",
    "risk_lucky_trade_short": "Lucky takas için eski veya uzakta yakalanmýþ Pokémonlarý bulur. Ama þans garanti deðildir.",
    "risk_lucky_trade_detailed": "Takas edildiðinde *lucky olma ihtimali yüksek* Pokémonlarý listeler. Ancak lucky oraný asla %100 deðildir. Ayrýca önceden takas edilmiþ bir Pokémon bir daha lucky olamaz. Listede deðerli Pokémonlar da çýkabilir.",
    "risk_lucky_trade_check1": "Önce arkadaþlýk seviyesini ve takas hakkýný kontrol et.",
    "risk_lucky_trade_check2": "Önceden takas edilmiþ bir Pokémon tekrar lucky olamaz.",
    "risk_lucky_trade_check3": "Emin deðilsen elinde tut.",
    "risk_untagged_title": "Neden hýzlýca kontrol etmelisin?",
    "risk_untagged_short": "Etiketsiz Pokémonlarý bulur. Çoðu güvenlidir ama araya parlak olanlar karýþabilir.",
    "risk_untagged_detailed": "Hiçbir etiketi olmayan Pokémonlarý gösterir. Genelde göndermek için güvenlidirler. Fakat etiketsiz kalmýþ parlak veya mükemmel bir Pokémon çýkabilir. Listeye hýzlýca göz at.",
    "risk_untagged_check1": "Sonuçlarýn hepsine hýzlýca bak.",
    "risk_untagged_check2": "Parlak, mükemmel veya emin olmadýklarýný sakla.",
    "risk_untagged_check3": "Deðerli olanlarý etiketleyip korumaya al.",
    "risk_expert_title": "Neden dikkat etmelisin?",
    "risk_expert_short": "Bu aramayý sen yazdýn. Ne yazdýysan birebir onu yapar.",
    "risk_expert_detailed": "Özel aramalar tam yazdýðýn gibi çalýþýr. Eðer yanlýþlýkla çok geniþ bir þey yazarsan deðerli Pokémonlar da çýkabilir. PokeQuery seni kendi yazdýðýn þeyden koruyamaz. Her sonucu kontrol et.",
    "risk_expert_check1": "Kendi yazdýðýn aramayý dikkatlice oku.",
    "risk_expert_check2": "Ýþlem yapmadan önce sonuçlara tek tek bak.",
    "risk_expert_check3": "Emin deðilsen elinde tut.",
    "risk_assistant_title": "Neden dikkat etmelisin?",
    "risk_assistant_short": "Asistan aramayý senin cümlelerinden oluþturdu. Sonuçlarý her zaman oyunda kontrol et.",
    "risk_assistant_detailed": "Asistan cümlelerini arama koduna çevirir. Kastediklerini tam yakalamayabilir veya istemediklerini de listeye katabilir. Bu sadece bir öneridir — sonuçlarla ne yapacaðýn sana kalmýþ.",
    "risk_assistant_check1": "Sonuçlarý her zaman Pokémon GO'da kontrol et.",
    "risk_assistant_check2": "Asistan hata yapabilir.",
    "risk_assistant_check3": "Emin deðilsen elinde tut.",
    "goal_detail_tip_title": "Ýpucu",
    "goal_detail_tip_desc": "Daha güvende olmak için toplu transfer öncesi rastgele birkaç sonucu gözden geçirin."
}

german_updates = {
    "risk_low_display": "Niedriges Risiko",
    "risk_medium_display": "Mittleres Risiko",
    "risk_low_subtitle": "Sicher zu verwenden",
    "risk_medium_subtitle": "Vor der Nutzung prüfen",
    "goal_detail_about_count": "Über die Anzahl (wichtig)",
    "goal_detail_about_count_desc": "Die Anzahl basiert auf der Spezies-Nummer. Shinys, Formen, Kostüme, Geschlecht oder Größe werden möglicherweise nicht unterschieden.",
    "goal_detail_what_does_this_do": "Was macht das?",
    "assistant_parse": "Generieren",
    "search_assistant_parse": "Generieren"
}

spanish_updates = {
    "risk_low_display": "Riesgo Bajo",
    "risk_medium_display": "Riesgo Medio",
    "risk_low_subtitle": "Seguro de usar",
    "risk_medium_subtitle": "Revisar antes de usar",
    "goal_detail_about_count": "Sobre la cantidad (importante)",
    "goal_detail_about_count_desc": "La cantidad se basa en el número de especie. Puede no distinguir entre variantes variocolor, formas, disfraces, género o tamaño.",
    "goal_detail_what_does_this_do": "¿Qué hace esto?",
    "assistant_parse": "Generar",
    "search_assistant_parse": "Generar"
}

french_updates = {
    "risk_low_display": "Risque Faible",
    "risk_medium_display": "Risque Moyen",
    "risk_low_subtitle": "Sûr à utiliser",
    "risk_medium_subtitle": "Vérifier avant utilisation",
    "goal_detail_about_count": "À propos du nombre (important)",
    "goal_detail_about_count_desc": "Le nombre est basé sur le numéro d'espèce. Il peut ne pas distinguer les chromatiques, formes, costumes, genres ou tailles.",
    "goal_detail_what_does_this_do": "Que fait ceci ?",
    "assistant_parse": "Générer",
    "search_assistant_parse": "Générer"
}

italian_updates = {
    "risk_low_display": "Rischio Basso",
    "risk_medium_display": "Rischio Medio",
    "risk_low_subtitle": "Sicuro da usare",
    "risk_medium_subtitle": "Controlla prima dell'uso",
    "goal_detail_about_count": "Riguardo al conteggio (importante)",
    "goal_detail_about_count_desc": "Il conteggio si basa sul numero della specie. Potrebbe non distinguere tra cromatici, forme, costumi, sesso o dimensioni.",
    "goal_detail_what_does_this_do": "Cosa fa questo?",
    "assistant_parse": "Genera",
    "search_assistant_parse": "Genera"
}

base_path = "app/src/main/res"
patch_file(f"{base_path}/values/strings.xml", english_updates)
patch_file(f"{base_path}/values-tr/strings.xml", turkish_updates)
patch_file(f"{base_path}/values-de/strings.xml", german_updates)
patch_file(f"{base_path}/values-es/strings.xml", spanish_updates)
patch_file(f"{base_path}/values-fr/strings.xml", french_updates)
patch_file(f"{base_path}/values-it/strings.xml", italian_updates)
