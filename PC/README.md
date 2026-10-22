# L1VO OS for PC — Foundation

This directory is the Windows PC track for L1VO OS. It is intentionally separate from the existing Android launcher source so desktop work cannot accidentally alter the Android app.

## Product direction

- Initial target: Windows 10 and Windows 11.
- Long-term goal: a standalone operating system.
- First milestone: a carefully designed Windows desktop shell/prototype.
- Design priority: preserve the established L1VO Android identity and use the original reference assets as the source of truth.

## Non-negotiable design constraints

1. **Control Cube geometry is locked.** Do not redraw, stretch, skew, or approximate the existing cube. Reuse the original artwork/vector/source geometry once located.
2. **Home Hub:** user-selected wallpaper is the canvas; L1VO controls sit above it.
3. **App Hub:** a purpose-designed application launcher, not a generic Windows Start-menu clone.
4. **No decorative divider lines.**
5. **No visible Windows desktop/wallpaper flash during navigation.** The prototype must own its surfaces and transitions consistently.
6. **Build in small, verifiable steps.** Keep PC changes on this branch and do not modify Android files as part of PC work.

## Technical path

A Windows application cannot replace the Windows kernel or become a standalone OS merely by being a desktop launcher. The safe incremental path is:

1. Create a Windows-native desktop-shell prototype that can run as a normal application.
2. Implement the visual system and Home Hub first; validate against the original L1VO screenshots.
3. Add the Control Cube interactions and App Hub without changing its geometry.
4. Add wallpaper persistence, app discovery/launching, settings, and startup behavior.
5. Only after the shell is stable, investigate a separate bootable OS distribution or Linux-based platform. Keep that work isolated from the Windows shell.

## First milestone: design fidelity

Before implementing the final UI, collect the authoritative Android reference images and the existing Control Cube asset from the Android project. Do not use generic concept art as a substitute for those references.

Acceptance checks:
- Cube proportions and symmetry match the approved reference.
- No unwanted divider lines.
- Home and App Hub layouts match their respective references.
- Wallpaper changes persist and navigation does not reveal the underlying desktop.
- Existing Android source remains unchanged by PC work.

## Current status

- [x] Dedicated PC development branch created: `feature/l1vo-pc-foundation`
- [x] PC track separated into this directory
- [ ] Add the authoritative Home Hub, App Hub, and Control Cube references
- [ ] Choose and scaffold the Windows UI framework after inspecting the existing assets and the development environment
- [ ] Build the first visual prototype and compare it against the references
