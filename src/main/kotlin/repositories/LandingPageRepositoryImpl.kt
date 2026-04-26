import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class LandingPageRepositoryImpl(
    private val collection: MongoCollection<LandingPage>
) : LandingPageRepository {

    override fun findDefault(): LandingPage? =
        collection.findOne(LandingPage::key eq LandingPage.DEFAULT_KEY)

    override fun upsertDefault(landingPage: LandingPage): LandingPage {
        val normalized = landingPage.copy(key = LandingPage.DEFAULT_KEY)
        collection.replaceOne(
            LandingPage::key eq LandingPage.DEFAULT_KEY,
            normalized,
            ReplaceOptions().upsert(true)
        )
        return normalized
    }
}
