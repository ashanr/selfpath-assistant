package com.libertyassistant.domain.usecase

import com.libertyassistant.domain.model.JournalEntry
import com.libertyassistant.domain.repository.IJournalRepository
import javax.inject.Inject

class SaveJournalEntryUseCase @Inject constructor(
    private val repository: IJournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Long = repository.saveEntry(entry)
}
