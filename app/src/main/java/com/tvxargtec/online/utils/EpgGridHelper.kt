package com.tvxargtec.online.utils

import android.content.Context
import com.tvxargtec.online.database.entity.EpgProgrammeEntity
import java.util.Calendar
import kotlin.random.Random

object EpgGridHelper {

    fun getWeekDays(): List<Pair<String, Long>> {
        val names = mapOf(
            Calendar.MONDAY to "Lun", Calendar.TUESDAY to "Mar",
            Calendar.WEDNESDAY to "Mie", Calendar.THURSDAY to "Jue",
            Calendar.FRIDAY to "Vie", Calendar.SATURDAY to "Sab",
            Calendar.SUNDAY to "Dom"
        )
        val cal = Calendar.getInstance()
        return (0 until 7).map { i ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, i)
            val startOfDay = Calendar.getInstance().apply {
                timeInMillis = c.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val name = if (i == 0) "Hoy" else names[c.get(Calendar.DAY_OF_WEEK)] ?: ""
            Pair(name, startOfDay)
        }
    }

    fun getTimeSlots(): List<String> = (6..23).map { "$it:00" }

    fun generateMockEpgData(context: Context, channelId: String, dayStart: Long): List<EpgProgrammeEntity> {
        val categories = listOf(
            "Noticias" to listOf("Noticiero Matutino", "El Mundo Hoy", "Reporte Especial", "Edicion Nocturna", "Primera Plana"),
            "Entretenimiento" to listOf("Talk Show", "Reality Show", "Concurso Familiar", "Espectaculos", "Variedades"),
            "Deportes" to listOf("Futbol en Vivo", "Resumen Deportivo", "Automovilismo", "Basquetbol", "Boxeo"),
            "Peliculas" to listOf("Cine de Accion", "Comedia Familiar", "Drama Intenso", "Ciencia Ficcion", "Thriller"),
            "Series" to listOf("Capitulo 1", "Capitulo 2", "Capitulo 3", "Capitulo 4", "Capitulo 5"),
            "Infantil" to listOf("Dibujos Animados", "Aventuras", "Aprende Jugando", "Cuentos", "Mascotas"),
            "Documentales" to listOf("Naturaleza Salvaje", "Historia Viva", "Tecnologia", "Universo", "Cultura"),
            "Musica" to listOf("Videos Musicales", "Concierto", "Top 40", "Clasicos", "En Vivo")
        )
        val programmes = mutableListOf<EpgProgrammeEntity>()
        val rng = Random(System.currentTimeMillis() + channelId.hashCode())
        var currentTime = dayStart + 6 * 3600 * 1000L
        val dayEnd = dayStart + 24 * 3600 * 1000L
        while (currentTime < dayEnd) {
            val (category, titles) = categories[rng.nextInt(categories.size)]
            val title = titles[rng.nextInt(titles.size)]
            val durationMinutes = listOf(30, 45, 60, 90, 120)[rng.nextInt(5)]
            val endTime = minOf(currentTime + durationMinutes * 60 * 1000L, dayEnd)
            programmes.add(EpgProgrammeEntity(
                channelId = channelId, title = title,
                description = "Disfruta de \"$title\" en tu canal favorito. Categoria: $category.",
                category = category, startTime = currentTime, endTime = endTime
            ))
            currentTime = endTime
        }
        return programmes
    }

    fun positionProgramme(startTime: Long, endTime: Long, dayStart: Long, slotDuration: Long): Pair<Float, Float> {
        val visStart = dayStart + 6 * 3600 * 1000L
        val visEnd = dayStart + 23 * 3600 * 1000L
        val visDuration = (visEnd - visStart).toFloat()
        val effStart = maxOf(startTime, visStart)
        val effEnd = minOf(endTime, visEnd)
        val left = (effStart - visStart) / visDuration
        val width = maxOf(0f, (effEnd - effStart) / visDuration)
        return Pair(left, width)
    }
}
