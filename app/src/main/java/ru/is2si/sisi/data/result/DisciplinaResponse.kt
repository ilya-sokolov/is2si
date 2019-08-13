package ru.is2si.sisi.data.result

import com.google.gson.annotations.SerializedName
import ru.is2si.sisi.domain.result.Disciplina

class DisciplinaResponse(
        @SerializedName("id")
        val id: Int,
        @SerializedName("CodeDiscipline")
        val codeDiscipline: String?,
        @SerializedName("IdVidSporta")
        val idVidSporta: Int,
        @SerializedName("Name")
        val name: String
)

fun DisciplinaResponse.toDisciplina() = Disciplina(
        id = id,
        codeDiscipline = codeDiscipline ?: "",
        idVidSporta = idVidSporta,
        name = name
)