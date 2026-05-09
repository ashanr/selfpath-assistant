package com.libertyassistant.domain.usecase

import com.libertyassistant.domain.model.JournalEntry
import com.libertyassistant.domain.repository.IJournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJournalEntriesUseCase @Inject constructor(
    private val repository: IJournalRepository
) {
    operator fun invoke(): Flow<List<JournalEntry>> = repository.getAllEntries()
}
