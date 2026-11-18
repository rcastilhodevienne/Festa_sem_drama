package com.example.data

import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.PartyEvent
import com.example.model.RsvpStatus

object PartyInitialData {

    val commonDietaryTags = listOf(
        "APLV",
        "Celíaco",
        "Vegetariano",
        "Sem Lactose",
        "Alergia a Amendoim"
    )

    val initialEvent = PartyEvent(
        id = "evt_leo_7",
        childName = "Leo",
        ageTurning = 7,
        title = "Aniversário de 7 Anos do Leo",
        subtitle = "Tarde de oficinas criativas, brincadeiras ao ar livre e bolo caseiro",
        dateDisplay = "Sábado, 05 de Dezembro",
        timeDisplay = "15h00 às 19h00",
        daysRemaining = 17,
        venueName = "Casa Quintal Espaço Brincar",
        fullAddress = "Rua Harmonia, 482 — Vila Madalena, São Paulo - SP",
        mapsQuery = "Rua Harmonia, 482, Vila Madalena, São Paulo",
        partyShareUrl = "https://festasemdrama.app/leo-7-anos",
        rsvpDeadline = "Confirmar até 28/11",
        hostSignature = "Com carinho, Mariana e Rafael"
    )

    val initialGuests = listOf(
        GuestGroup(
            id = "gst_1",
            primaryGuestName = "Camila & Lucas Ferreira",
            adultsCount = 2,
            childrenCount = 1,
            phoneWhatsApp = "11998421034",
            status = RsvpStatus.CONFIRMED,
            dietaryTags = listOf("APLV"),
            dietaryNotes = "O pequeno Theo (4 anos) tem APLV severa — sem traços de leite."
        ),
        GuestGroup(
            id = "gst_2",
            primaryGuestName = "Marina Oliveira",
            adultsCount = 1,
            childrenCount = 2,
            phoneWhatsApp = "11997315520",
            status = RsvpStatus.CONFIRMED,
            dietaryTags = listOf("Vegetariano"),
            dietaryNotes = "Marina é vegetariana; as crianças comem de tudo."
        ),
        GuestGroup(
            id = "gst_3",
            primaryGuestName = "Ricardo & Bia Mendes",
            adultsCount = 2,
            childrenCount = 1,
            phoneWhatsApp = "11988124409",
            status = RsvpStatus.CONFIRMED,
            dietaryTags = listOf("Celíaco"),
            dietaryNotes = "Bia é celíaca — precisa de opções 100% sem glúten."
        ),
        GuestGroup(
            id = "gst_4",
            primaryGuestName = "Fernanda Costa (Tia Fê)",
            adultsCount = 2,
            childrenCount = 0,
            phoneWhatsApp = "11991047822",
            status = RsvpStatus.PENDING,
            dietaryTags = emptyList(),
            dietaryNotes = ""
        ),
        GuestGroup(
            id = "gst_5",
            primaryGuestName = "Thiago & Juliana Prado",
            adultsCount = 2,
            childrenCount = 2,
            phoneWhatsApp = "11996503187",
            status = RsvpStatus.CONFIRMED,
            dietaryTags = listOf("APLV", "Alergia a Amendoim"),
            dietaryNotes = "Sofia tem APLV leve e alergia a amendoim/castanhas."
        ),
        GuestGroup(
            id = "gst_6",
            primaryGuestName = "Patrícia Nogueira",
            adultsCount = 1,
            childrenCount = 1,
            phoneWhatsApp = "11983441905",
            status = RsvpStatus.PENDING,
            dietaryTags = listOf("Vegetariano"),
            dietaryNotes = "Mãe e filho vegetarianos."
        ),
        GuestGroup(
            id = "gst_7",
            primaryGuestName = "Bruno & Carla Ribeiro",
            adultsCount = 2,
            childrenCount = 1,
            phoneWhatsApp = "11992208761",
            status = RsvpStatus.PENDING,
            dietaryTags = emptyList(),
            dietaryNotes = ""
        ),
        GuestGroup(
            id = "gst_8",
            primaryGuestName = "Eduardo & Aline Santos",
            adultsCount = 2,
            childrenCount = 1,
            phoneWhatsApp = "11984776012",
            status = RsvpStatus.DECLINED,
            dietaryTags = emptyList(),
            dietaryNotes = "Estarão viajando no feriado."
        )
    )

    val initialGifts = listOf(
        GiftItem(
            id = "gft_1",
            name = "LEGO City Estação Espacial Modular",
            category = GiftCategory.TOYS,
            sizeOrModelSuggestion = "Modelo 60433 · 7+ anos",
            details = "Leo adora montar naves e planetas.",
            reservedByGuestName = "Camila & Lucas Ferreira"
        ),
        GiftItem(
            id = "gft_2",
            name = "Tênis Infantil Estilo Skate",
            category = GiftCategory.CLOTHING,
            sizeOrModelSuggestion = "Tam. 31 (BR) · Cor azul marinho ou preto",
            details = "Preferência por modelos com velcro ou cadarço elástico.",
            reservedByGuestName = null
        ),
        GiftItem(
            id = "gft_3",
            name = "Atlas Ilustrado do Universo e Dinossauros",
            category = GiftCategory.BOOKS,
            sizeOrModelSuggestion = "Capa dura · Editora Zahar ou Brinque-Book",
            details = "Leitura compartilhada antes de dormir.",
            reservedByGuestName = "Marina Oliveira"
        ),
        GiftItem(
            id = "gft_4",
            name = "Vale-Passeio Aquário de São Paulo (2 ingressos)",
            category = GiftCategory.EXPERIENCES,
            sizeOrModelSuggestion = "Voucher digital ou impresso",
            details = "Experiência em família para as férias de janeiro.",
            reservedByGuestName = null
        ),
        GiftItem(
            id = "gft_5",
            name = "Patinete Dobrável 3 Rodas com LED",
            category = GiftCategory.TOYS,
            sizeOrModelSuggestion = "Suporta até 50kg · Altura ajustável",
            details = "Ideal para os passeios no parque aos domingos.",
            reservedByGuestName = "Ricardo & Bia Mendes"
        ),
        GiftItem(
            id = "gft_6",
            name = "Conjunto Pijama Algodão Espaço Sideral",
            category = GiftCategory.CLOTHING,
            sizeOrModelSuggestion = "Tam. 8 anos · 100% algodão",
            details = "Pode ser manga curta ou longa leve.",
            reservedByGuestName = null
        ),
        GiftItem(
            id = "gft_7",
            name = "Box Diário de um Banana (Volumes 1 a 3)",
            category = GiftCategory.BOOKS,
            sizeOrModelSuggestion = "Edição em Português · Capa brochura",
            details = "Ótimo para incentivar a leitura independente aos 7 anos.",
            reservedByGuestName = null
        ),
        GiftItem(
            id = "gft_8",
            name = "Oficina de Cerâmica Infantil (1 sessão)",
            category = GiftCategory.EXPERIENCES,
            sizeOrModelSuggestion = "Ateliê na Vila Madalena · Sábado manhã",
            details = "Ele pediu muito para aprender a fazer canecas no torno.",
            reservedByGuestName = "Thiago & Juliana Prado"
        ),
        GiftItem(
            id = "gft_9",
            name = "Jogo de Tabuleiro Detetive Júnior",
            category = GiftCategory.TOYS,
            sizeOrModelSuggestion = "Estrela · 7+ anos",
            details = "Para jogar em família nos dias de chuva.",
            reservedByGuestName = null
        )
    )
}
