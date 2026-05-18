# Editorial UI QA Checklist (Phase 1)

Use this checklist before merging UI work to keep the Flat Journal design language consistent.

## 1) Component Usage
- Use `EditorialPrimaryButton` / `EditorialSecondaryButton` for screen actions.
- Use `EditorialTextField` for form inputs unless a screen has a validated exception.
- Use `EditorialCard` or `JournalPanel` for grouped content surfaces.
- Use `EditorialTabs` instead of raw `TabRow` in standard tab sections.

## 2) Typography and Tone
- Section headers should use `EditorialSectionTitle` with a subtitle label.
- Body copy should avoid long dense paragraphs inside cards.
- Metadata and utility labels should use concise editorial phrasing.

## 3) Layout Rhythm
- Keep vertical spacing on an 8/12/16/24 dp rhythm.
- Avoid placing more than two primary actions in one horizontal row.
- Do not mix large-radius and flat-radius components in the same block.

## 4) Color and Contrast
- Primary accent should appear in headings, key actions, and selection states.
- Decorative accents should remain subtle (alpha-based, not fully saturated fills).
- Text and icon contrast should remain readable in both light and dark themes.

## 5) Interaction Consistency
- Buttons should clearly indicate primary vs secondary intent.
- Empty and loading states should use the same panel/card language.
- Navigation regions should preserve consistent border and panel treatment.
