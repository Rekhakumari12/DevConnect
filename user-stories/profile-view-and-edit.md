# User Story – Profile (View & Edit My Profile, View Others’ Public Profiles)

## Business Context

DevConnect relies on meaningful developer profiles so that ideas and discussions are discoverable and credible. Each developer needs an easy way to review and update their own skills and bio, while other developers should be able to view high-level public information about them when exploring posts and comments. At the same time, contact details such as email should remain private by default and never exposed to other users. This story delivers the end-to-end profile experience for:

- Viewing and editing one’s own profile (skills and bio),
- Viewing another developer’s public profile by navigating from their content or by username,
- Ensuring email is only visible to the profile owner and is not shown to others.

---

## Story Text

As a developer participating in DevConnect,
I want to review and update my own profile and view other developers’ public profiles,
So that I can present my skills clearly and understand who I am interacting with, without exposing private contact details.

---

## Acceptance Criteria

### 1. View My Own Profile – Happy Path

- **Given** I am signed in as a developer  
  **When** I open the "My profile" view from navigation or a prominent entry point  
  **Then** I see my current profile information, including username, email, skills, and bio  
  **And** it is visually clear which fields I can change (skills and bio) and which are fixed (username and email).

### 2. Edit My Profile (Skills & Bio Only)

- **Given** I am signed in and viewing my own profile  
  **When** I update my skills (for example, add, remove, or change them) and/or edit my bio and save the changes  
  **Then** the changes are successfully stored  
  **And** I see an updated view of my profile reflecting the new skills and bio  
  **And** I receive clear feedback that the update was successful.

- **Given** I am signed in and viewing my own profile  
  **When** I attempt to change fixed identity information such as my username or email  
  **Then** the interface does not allow me to change these values as part of this profile flow  
  **And** it is clear from the layout and controls that these values are read-only.

### 3. Profile Update – Validation and Error Handling

- **Given** I am editing my profile  
  **When** I attempt to save changes that violate basic rules enforced on the page (for example, an empty skills list or a bio that exceeds an allowed length, if such limits are defined)  
  **Then** I see clear, inline validation messages indicating what must be corrected  
  **And** my changes are not applied until the issues are resolved.

- **Given** I submit valid profile updates (skills and/or bio)  
  **When** the server rejects the update due to its own validation rules or a temporary failure  
  **Then** I see a friendly error message explaining that the update could not be completed  
  **And** my attempted changes remain visible in the form so that I can adjust and retry without retyping everything.

### 4. Email Visibility for Profile Owner

- **Given** I am signed in and viewing my own profile  
  **When** I look at my profile details  
  **Then** I can see my own email address in a way that makes it clear that it is part of my account information  
  **And** it is visually distinct from information that is visible to other developers.

- **Given** I am signed in and viewing my own profile  
  **When** I choose to reveal or hide my email within the page (for example, through a simple show/hide interaction)  
  **Then** this reveal only affects what I can see on my own screen  
  **And** it does not cause my email to become visible to other developers viewing my profile.

### 5. View Another Developer’s Public Profile – From Content

- **Given** I am viewing a public list of posts, a specific post, or a list of comments  
  **When** I click on a developer’s name or avatar associated with a piece of content  
  **Then** I am taken to that developer’s public profile page  
  **And** this works whether or not I am currently signed in.

- **Given** I am viewing another developer’s public profile  
  **When** I review the information shown  
  **Then** I can see high-level public details such as their username, skills, and bio  
  **And** I cannot see their email address or any explicit contact details that are considered private.

### 6. View Another Developer’s Public Profile – By Username Lookup

- **Given** I am anywhere in the application  
  **When** I use a dedicated way to look up a profile by username (for example, a simple input and action labeled accordingly)  
  **Then** entering a valid username takes me to that developer’s public profile page  
  **And** I see the same public information as when navigating from posts or comments.

- **Given** I am using the username lookup to find a profile  
  **When** I enter a username that does not exist  
  **Then** I receive a clear message that no matching developer profile was found  
  **And** I am given a way to correct the username or return to my previous view.

### 7. Access Control – My Profile vs Others’ Profiles

- **Given** I am not signed in  
  **When** I try to access my own profile view (for example, via a direct URL or a protected navigation link)  
  **Then** I am not allowed to see a personal profile view  
  **And** I am prompted to sign in first.

- **Given** I am not signed in  
  **When** I visit another developer’s public profile  
  **Then** I can still see their public information (username, skills, bio)  
  **And** there is no option to edit anything on that profile.

- **Given** I am signed in as one developer  
  **When** I visit another developer’s profile page  
  **Then** I see the public view only  
  **And** I do not see controls or indicators that would allow me to edit their profile.

---

## Out of Scope

- Changing username or email address through the profile page.
- Managing or displaying advanced privacy settings beyond not showing email to others.
- Surfacing a list of all developers or searching by skills or bio.
- Any admin-only profile views or actions.

---

## Dependencies

- A capability on the backend to provide the signed-in developer’s own profile details and accept updates to skills and bio.
- A capability on the backend to provide a public view of a developer’s profile by username, without exposing private contact details.
- Existing navigation that can distinguish between "My profile" and another developer’s profile.
- Consistent patterns for showing validation messages and server errors across the application.

---

## Assumptions

- The backend enforces which fields are allowed to change as part of profile updates, and will reject changes to identity fields such as username and email.
- Email is always treated as private contact information and is never included in responses that represent someone else’s public profile.
- A simple, non-discoverability feature (such as a direct username lookup) is sufficient for navigating to a specific developer’s profile in addition to links from posts and comments.
- Viewing another developer’s profile is allowed even for visitors who are not signed in.

---

## Mockups / Supporting Diagrams

### My Profile Page – Conceptual Layout

- Sections:
  - Header with my display identifier (for example, username) and a clear label that this is my own profile.
  - Read-only section for fixed information such as username and email.
  - Editable section for skills (for example, tag list or comma-separated input) and bio (multi-line text).
- Actions:
  - Primary action to save changes to skills and bio.
  - Optional secondary action to cancel edits and revert to the last saved state.
- States:
  - Normal view mode with read-only display of current information.
  - Edit mode where skills and bio become editable controls.
  - Error and success banners aligned with how other pages show feedback.

### Public Profile Page (Other Developer) – Conceptual Layout

- Sections:
  - Header with the developer’s username and a clear indication that this is a public profile.
  - Public details such as skills and bio.
  - Optional link to view this developer’s public posts, if such a listing is available elsewhere.
- Restrictions:
  - No email address or private contact details are shown.
  - No edit controls are visible for viewers other than the profile owner.

### High-Level Interaction Flow – From Content to Profile

- While browsing posts or comments, each developer’s name or avatar acts as an entry point to their public profile.
- Clicking this entry navigates to a profile view that shows public information only.
- From my own navigation (for example, a header menu), selecting "My profile" always opens my personal editable view, distinct from the public view used for other developers.
