//> using dep "com.softwaremill.sttp.client4::core:4.0.0-M8"
//> using dep com.lihaoyi::upickle:3.1.4
// sttp is part of the scala toolkit so we could also just bring in the toolkit
import sttp.client4.quick._
import sttp.client4.Response
import upickle.default._
import upickle.implicits.key

case class CatFact(status: FactStatus, text: String, @key("type") animal: String)
    derives ReadWriter

case class FactStatus(verified: Boolean)
    derives ReadWriter


@main def run: Unit = 

    // https://cat-fact.herokuapp.com/facts/
    // curl --location 'https://openlibrary.org/api/books?bibkeys=ISBN%3A0201558025%2CLCCN%3A93005405&format=json'

    val response = quickRequest.get(uri"https://cat-fact.herokuapp.com/facts/").send()
    // This is json string
    val responseBody = response.body
    val catFacts: List[CatFact] = upickle.default.read[List[CatFact]](responseBody)

    for (fact <- catFacts) do println(s">> ${fact.text}\n>>>> This fact is verified to be: ${fact.status.verified}\n")

